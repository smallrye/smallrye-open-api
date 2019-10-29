package io.smallrye.openapi.runtime.scanner;

import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_BEAN_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_COOKIE_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_DEFAULT_VALUE;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_DEPRECATED;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_FORM_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_HEADER_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_JAXRS_HTTP_METHODS;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_MATRIX_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_PARAMETER;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_PARAMETERS;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_PATH;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_PATH_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_QUERY_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_RESTEASY_COOKIE_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_RESTEASY_FORM_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_RESTEASY_HEADER_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_RESTEASY_MATRIX_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_RESTEASY_MULTIPART_FORM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_RESTEASY_PART_TYPE;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_RESTEASY_PATH_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.DOTNAME_RESTEASY_QUERY_PARAM;
import static io.smallrye.openapi.api.OpenApiConstants.PROP_VALUE;
import static io.smallrye.openapi.api.util.MergeUtil.mergeObjects;
import static io.smallrye.openapi.runtime.util.JandexUtil.getMethodParameterType;
import static io.smallrye.openapi.runtime.util.JandexUtil.stringValue;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.In;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.PrimitiveType.Primitive;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.EncodingImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.scanner.dataobject.BeanValidationScanner;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.SchemaFactory;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 *
 * Note, {@link javax.ws.rs.PathParam PathParam} targets of
 * {@link javax.ws.rs.core.PathSegment PathSegment} are not currently supported.
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 *
 */
// TODO: Treat PathSegment params as matrix parameters
public class ParameterProcessor {

    private static final Logger LOG = Logger.getLogger(ParameterProcessor.class);

    private static Comparator<ParameterContextKey> parameterComparator = Comparator.comparing(ParameterContextKey::getLocation,
            Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(ParameterContextKey::getName,
                    Comparator.nullsLast(Comparator.reverseOrder()));

    private static Set<DotName> openApiParameterAnnotations = new HashSet<>(Arrays.asList(DOTNAME_PARAMETER,
            DOTNAME_PARAMETERS));

    private final IndexView index;
    private final Function<AnnotationInstance, ParameterImpl> reader;
    private final List<AnnotationScannerExtension> extensions;

    /**
     * Collection of parameters scanned at the current level. This map contains
     * all parameter types except for form parameters and JAX-RS {@link javax.ws.rs.MatrixParam MatrixParam}s.
     */
    private Map<ParameterContextKey, ParameterContext> params = new TreeMap<>(parameterComparator);

    /**
     * Collection of JAX-RS {@link javax.ws.rs.FormParam FormParam}s found during scanning.
     * These annotations will be used as schema properties for a generated schema used in the
     * MP-OAI {@link org.eclipse.microprofile.openapi.models.responses.APIResponse APIResponse}
     * if a value has not be provided by the application.
     */
    private Map<String, AnnotationInstance> formParams = new LinkedHashMap<>();

    /**
     * The media type of a form schema found while scanning the parameters.
     */
    private String formMediaType;

    /**
     * Collection of JAX-RS {@link javax.ws.rs.MatrixParam MatrixParam}s found during scanning.
     * These annotations will be used as schema properties for the schema of a path parameter
     * having {@link Parameter#setStyle style} of {@link Style#MATRIX}.
     */
    private Map<String, Map<String, AnnotationInstance>> matrixParams = new LinkedHashMap<>();

    /**
     * Result object returned to the annotation scanner. Parameters are split between
     * those that apply at the {@link PathItem} level and those that apply at the
     * {@link Operation} level, except for form parameters which only apply to the operation.
     *
     * This object includes the class and method path which may have been modified from the
     * values specified by {@link javax.ws.rs.Path Path} annotations to support the linkage of
     * matrix parameters.
     *
     * @author Michael Edgar {@literal <michael@xlate.io>}
     *
     */
    public static class ResourceParameters {
        private String pathItemPath;
        private List<Parameter> pathItemParameters;

        private String operationPath;
        private List<Parameter> operationParameters;

        private Content formBodyContent;

        public List<Parameter> getPathItemParameters() {
            return pathItemParameters;
        }

        public String getOperationPath() {
            return pathItemPath + operationPath;
        }

        public List<Parameter> getOperationParameters() {
            return operationParameters;
        }

        public Content getFormBodyContent() {
            return formBodyContent;
        }

        public Schema getFormBodySchema() {
            if (formBodyContent != null) {
                return formBodyContent.getMediaTypes().values().iterator().next().getSchema();
            }
            return null;
        }

        /* Internal setters */
        void setPathItemPath(String pathItemPath) {
            this.pathItemPath = pathItemPath;
        }

        void setPathItemParameters(List<Parameter> pathItemParameters) {
            this.pathItemParameters = pathItemParameters;
        }

        void setOperationPath(String operationPath) {
            this.operationPath = operationPath;
        }

        void setOperationParameters(List<Parameter> operationParameters) {
            this.operationParameters = operationParameters;
        }

        void setFormBodyContent(Content formBodyContent) {
            this.formBodyContent = formBodyContent;
        }
    }

    /**
     * Meta information for the JAX-RS *Param annotations relating them
     * to the {@link In} and {@link Style} attributes of {@link Parameter}s.
     *
     * @author Michael Edgar {@literal <michael@xlate.io>}
     */
    public enum JaxRsParameter {
        PATH_PARAM(DOTNAME_PATH_PARAM, In.PATH, null),
        // Apply to the last-matched @Path of the structure injecting the MatrixParam
        MATRIX_PARAM(DOTNAME_MATRIX_PARAM, In.PATH, Style.MATRIX),
        QUERY_PARAM(DOTNAME_QUERY_PARAM, In.QUERY, null),
        FORM_PARAM(DOTNAME_FORM_PARAM, null, Style.FORM),
        HEADER_PARAM(DOTNAME_HEADER_PARAM, In.HEADER, null),
        COOKIE_PARAM(DOTNAME_COOKIE_PARAM, In.COOKIE, null),
        BEAN_PARAM(DOTNAME_BEAN_PARAM, null, null),

        // Support RESTEasy annotations directly
        RESTEASY_PATH_PARAM(DOTNAME_RESTEASY_PATH_PARAM, In.PATH, null),
        // Apply to the last-matched @Path of the structure injecting the MatrixParam
        RESTEASY_MATRIX_PARAM(DOTNAME_RESTEASY_MATRIX_PARAM, In.PATH, Style.MATRIX),
        RESTEASY_QUERY_PARAM(DOTNAME_RESTEASY_QUERY_PARAM, In.QUERY, null),
        RESTEASY_FORM_PARAM(DOTNAME_RESTEASY_FORM_PARAM, null, Style.FORM),
        RESTEASY_HEADER_PARAM(DOTNAME_RESTEASY_HEADER_PARAM, In.HEADER, null),
        RESTEASY_COOKIE_PARAM(DOTNAME_RESTEASY_COOKIE_PARAM, In.COOKIE, null),
        RESTEASY_MULITIPART_FORM(DOTNAME_RESTEASY_MULTIPART_FORM, null, null, MediaType.MULTIPART_FORM_DATA);

        private final DotName name;
        final In location;
        final Style style;
        final String mediaType;

        private JaxRsParameter(DotName name, In location, Style style, String mediaType) {
            this.name = name;
            this.location = location;
            this.style = style;
            this.mediaType = mediaType;
        }

        private JaxRsParameter(DotName name, In location, Style style) {
            this(name, location, style, null);
        }

        static JaxRsParameter forName(DotName annotationName) {
            for (JaxRsParameter value : values()) {
                if (value.name.equals(annotationName)) {
                    return value;
                }
            }
            return null;
        }

        public static boolean isParameter(DotName annotationName) {
            for (JaxRsParameter value : values()) {
                if (value.name.equals(annotationName)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Used for collecting and merging any scanned {@link Parameter} annotations
     * with the JAX-RS *Param annotations. After scanning, this object may
     * contain either the MP-OAI annotation information, the JAX-RS annotation
     * information, or both.
     *
     * @author Michael Edgar {@literal <michael@xlate.io>}
     */
    static class ParameterContext {
        String name;
        In location;
        ParameterImpl oaiParam;
        JaxRsParameter jaxRsParam;
        Object jaxRsDefaultValue;
        AnnotationTarget target;
        Type targetType;

        @Override
        public String toString() {
            return "name: " + name + "; in: " + location + "; target: " + target;
        }
    }

    /**
     * Key used to store {@link ParameterContext} objects in a map sorted by {@link In},
     * then by name, nulls first.
     *
     * @author Michael Edgar {@literal <michael@xlate.io>}
     *
     */
    static class ParameterContextKey {
        String name;
        In location;

        ParameterContextKey(String name, In location) {
            this.name = name;
            this.location = location;
        }

        ParameterContextKey(ParameterContext context) {
            this.name = context.name;
            this.location = context.location;
        }

        @Override
        public String toString() {
            return "name: " + name + "; in: " + location;
        }

        public String getName() {
            return name;
        }

        public In getLocation() {
            return location;
        }
    }

    private ParameterProcessor(IndexView index,
            Function<AnnotationInstance, ParameterImpl> reader,
            List<AnnotationScannerExtension> extensions) {
        this.index = index;
        this.reader = reader;
        this.extensions = extensions;
    }

    /**
     * Process parameter annotations for the given class and method. This method operates
     * in two phases. First, class-level parameters are processed and saved in the
     * {@link ResourceParameters}. Second, method-level parameters are processed. Form parameters
     * are only applicable to the method-level in this component.
     *
     * @param index index of classes to be used for further introspection, if necessary
     * @param resourceMethod the JAX-RS resource method, annotated with one of the
     *        JAX-RS HTTP annotations
     * @param reader callback method for a function producing {@link ParameterImpl} from a
     *        {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter}
     * @param extensions scanner extensions
     * @return scanned parameters and modified path contained in a {@link ResourceParameters}
     *         object
     */
    public static ResourceParameters process(IndexView index,
            ClassInfo resourceClass,
            MethodInfo resourceMethod,
            Function<AnnotationInstance, ParameterImpl> reader,
            List<AnnotationScannerExtension> extensions) {

        ResourceParameters parameters = new ResourceParameters();
        ParameterProcessor processor = new ParameterProcessor(index, reader, extensions);

        ClassInfo resourceMethodClass = resourceMethod.declaringClass();

        /*
         * Phase I - Read class fields, constructors, "setter" methods not annotated with JAX-RS
         * HTTP method. Check both the class declaring the method as well as the resource
         * class, if different.
         */
        processor.readParameters(resourceMethodClass, null);
        if (!resourceClass.equals(resourceMethodClass)) {
            /*
             * The resource class may be a subclass/implementor of the resource method class. Scanning
             * the resource class after the method's class allows for parameter details to be overridden
             * by annotations in the subclass.
             */
            processor.readParameters(resourceClass, null);
        }

        parameters.setPathItemParameters(processor.getParameters());
        /*
         * Generate the path using the provided resource class, which may differ from the method's declaring
         * class - e.g. for inheritance.
         */
        parameters.setPathItemPath(processor.generatePath(resourceClass, parameters.getPathItemParameters()));

        // Clear Path-level parameters discovered and allows for processing operation-level parameters
        processor.reset();

        // Phase II - Read method argument @Parameter and JAX-RS *Param annotations
        resourceMethod.annotations()
                .stream()
                .filter(a -> !a.target().equals(resourceMethod))
                // TODO: Replace 'forEach' below with ".forEach(processor::readAnnotatedType);" once @Parameters no longer need to be supported on method arguments
                .forEach(annotation -> {
                    /*
                     * This condition exists to support @Parameters wrapper annotation
                     * on method parameters until (if?) the MP-OAI TCK is changed.
                     */
                    if (openApiParameterAnnotations.contains(annotation.name())) {
                        processor.readParameterAnnotation(annotation);
                    } else {
                        processor.readAnnotatedType(annotation);
                    }
                });

        // Phase III - Read method @Parameter(s) annotations
        resourceMethod.annotations()
                .stream()
                .filter(a -> a.target().equals(resourceMethod))
                .filter(a -> openApiParameterAnnotations.contains(a.name()))
                .forEach(processor::readParameterAnnotation);

        parameters.setOperationParameters(processor.getParameters());
        parameters.setOperationPath(processor.generatePath(resourceMethod, parameters.getOperationParameters()));

        parameters.setFormBodyContent(processor.getFormBodyContent());

        return parameters;
    }

    private void reset() {
        params.clear();
        formParams.clear();
        matrixParams.clear();
    }

    /**
     * Generate the path for the provided annotation target, either a class or a method.
     * Add the name of any discovered matrix parameters.
     *
     * @param target the target (either class or method)
     * @param parameters
     * @return
     */
    // TODO: Parse path segments to strip out variable names and patterns (apply patterns to path param schema)
    String generatePath(AnnotationTarget target, List<Parameter> parameters) {
        String path = pathOf(target);

        if (path.length() > 0) {
            path = '/' + path;
        }

        if (matrixParams.size() > 0) {
            String matrixName = parameters.stream()
                    .filter(p -> p.getStyle() == Style.MATRIX)
                    .map(Parameter::getName)
                    .findFirst()
                    .orElse("");

            // If it's empty, something went wrong
            if (matrixName.length() > 0) {
                path = path + '{' + matrixName + '}';
            } else {
                LOG.debugf("Matrix parameter was blank for path %s, target %s", path, target.toString());
            }
        }

        return path;
    }

    /**
     * Performs the final merging of JAX-RS parameters with MP-OAI parameters to produce the list
     * of {@link Parameter}s found while scanning the current level (class or method).
     *
     * @return list of {@link Parameter}s
     */
    private List<Parameter> getParameters() {
        List<Parameter> parameters = new ArrayList<>();

        // Process any Matrix Parameters found
        if (matrixParams.size() > 0) {
            for (Entry<String, Map<String, AnnotationInstance>> matrixPath : matrixParams.entrySet()) {
                String contextPath = matrixPath.getKey();

                //Find a ParamContext style=matrix at this path
                ParameterContext context = params.values()
                        .stream()
                        .filter(p -> p.oaiParam != null)
                        .filter(p -> p.oaiParam.getStyle() == Style.MATRIX)
                        .filter(p -> contextPath.equals(fullPathOf(p.target)))
                        .findFirst()
                        .orElse(null);

                if (context == null) {
                    /*
                     * @Parameter style=matrix was not specified at the same @Path segment of this @MatrixParam
                     * Generate one here
                     */
                    context = new ParameterContext();

                    String finalSegment = contextPath.substring(contextPath.lastIndexOf('/') + 1);

                    if (finalSegment.startsWith("{") && finalSegment.endsWith("}")) {
                        finalSegment = finalSegment.substring(1, finalSegment.length() - 1);
                    }

                    context.name = finalSegment + "Matrix";
                    context.location = In.PATH;
                    context.jaxRsParam = JaxRsParameter.MATRIX_PARAM;
                    context.target = null;
                    context.targetType = null;
                    context.oaiParam = new ParameterImpl();
                    context.oaiParam.explode(Boolean.TRUE);
                    params.put(new ParameterContextKey(context), context);
                }

                List<Schema> schemas = ModelUtil.getParameterSchemas(context.oaiParam);

                if (schemas.isEmpty()) {
                    // ParameterContext was generated above or no @Schema was provided on the @Parameter style=matrix
                    Schema schema = new SchemaImpl();
                    schema.setType(SchemaType.OBJECT);
                    ModelUtil.setParameterSchema(context.oaiParam, schema);
                    schemas = Arrays.asList(schema);
                }

                for (Schema schema : schemas) {
                    setSchemaProperties(schema, Collections.emptyMap(), matrixPath.getValue());
                }
            }
        }

        // Convert ParameterContext entries to MP-OAI Parameters
        for (ParameterContext context : params.values()) {
            ParameterImpl param;

            if (context.oaiParam == null) {
                param = new ParameterImpl();
            } else {
                param = context.oaiParam;
            }

            if (param.getName() == null) {
                param.setName(context.name);
            }

            if (param.getIn() == null && context.location != null) {
                param.setIn(context.location);
            }

            if (isIgnoredParameter(param)) {
                continue;
            }

            if (param.getIn() == In.PATH) {
                param.setRequired(true);
            }

            if (param.getStyle() == null && context.jaxRsParam != null) {
                param.setStyle(context.jaxRsParam.style);
            }

            if (!ModelUtil.parameterHasSchema(param) && context.targetType != null) {
                Schema schema = SchemaFactory.typeToSchema(index, context.targetType, extensions);
                ModelUtil.setParameterSchema(param, schema);
            }

            if (param.getDeprecated() == null) {
                if (TypeUtil.getAnnotation(context.target, DOTNAME_DEPRECATED) != null) {
                    param.setDeprecated(Boolean.TRUE);
                }
            }

            if (param.getSchema() != null) {
                //TODO: Test BV annotations on all target types
                BeanValidationScanner.applyConstraints(context.target,
                        param.getSchema(),
                        param.getName(),
                        (target, name) -> {
                            if (param.getRequired() == null) {
                                param.setRequired(Boolean.TRUE);
                            }
                        });

                if (param.getSchema().getDefaultValue() == null) {
                    param.getSchema().setDefaultValue(context.jaxRsDefaultValue);
                }
            }

            parameters.add(param);
        }

        return parameters.isEmpty()
                ? null
                : parameters.stream()
                        .sorted(Comparator.comparing(Parameter::getIn)
                                .thenComparing(Parameter::getName))
                        .collect(Collectors.toList());
    }

    /**
     * Create a {@link Content} and use the scanned {@link javax.ws.rs.FormParam}s
     * as the properties. The media type will be defaulted to
     * 'application/x-www-form-urlencoded' or set to 'multipart/form-data' if a
     * RESTEasy {@link org.jboss.resteasy.annotations.providers.multipart.MultipartForm MultipartForm}
     * annotation was used to wrap the {@link javax.ws.rs.FormParam}s. The encoding values
     * for the {@link Content} will be set to the value of any
     * {@link org.jboss.resteasy.annotations.providers.multipart.PartType PartType}
     * annotations found for each parameter.
     *
     * @return generated form content
     */
    private Content getFormBodyContent() {
        if (formParams.isEmpty()) {
            return null;
        }

        Content content = new ContentImpl();
        MediaTypeImpl mediaType = new MediaTypeImpl();
        Schema schema = new SchemaImpl();
        Map<String, Encoding> encodings = new HashMap<>();
        schema.setType(SchemaType.OBJECT);

        mediaType.setSchema(schema);
        setSchemaProperties(schema, encodings, formParams);

        if (encodings.size() > 0) {
            mediaType.setEncoding(encodings);
        }

        String mediaTypeName = formMediaType != null ? formMediaType : MediaType.APPLICATION_FORM_URLENCODED;
        content.addMediaType(mediaTypeName, mediaType);

        return content;
    }

    /**
     * Converts the collection of parameter annotations to properties set on the
     * given schema.
     *
     * @param schema the {@link Schema} on which the properties will be set
     * @param encodings map of encodings applicable to the current {@link MediaType} being processed
     * @param params the name/value pairs of annotations for conversion to schema properties
     */
    void setSchemaProperties(Schema schema,
            Map<String, Encoding> encodings,
            Map<String, AnnotationInstance> params) {

        for (Entry<String, AnnotationInstance> param : params.entrySet()) {
            String paramName = param.getKey();
            AnnotationTarget paramTarget = param.getValue().target();
            addEncoding(encodings, paramName, paramTarget);
            Type paramType = getType(paramTarget);
            Schema paramSchema = SchemaFactory.typeToSchema(index, paramType, extensions);
            Object defaultValue = getDefaultValue(paramTarget);

            if (paramSchema.getDefaultValue() == null) {
                paramSchema.setDefaultValue(defaultValue);
            }

            BeanValidationScanner.applyConstraints(paramTarget,
                    paramSchema,
                    paramName,
                    (target, name) -> {
                        List<String> requiredProperties = schema.getRequired();

                        if (requiredProperties == null || !requiredProperties.contains(name)) {
                            schema.addRequired(name);
                        }
                    });

            if (schema.getProperties() != null) {
                paramSchema = mergeObjects(schema.getProperties().get(paramName), paramSchema);
            }
            schema.addProperty(paramName, paramSchema);
        }
    }

    /**
     * Determine if the paramTarget is annotated with the RestEasy
     * {@link org.jboss.resteasy.annotations.providers.multipart.PartType @PartType}
     * annotation and add the value to the encodings map.
     *
     * @param encodings map of encodings applicable to the current {@link MediaType} being processed
     * @param paramName name of the current form parameter being mapped to a schema property
     * @param paramTarget the target annotated with {@link javax.ws.rs.FormParam FormParam}
     *
     */
    static void addEncoding(Map<String, Encoding> encodings, String paramName, AnnotationTarget paramTarget) {
        if (paramTarget == null) {
            return;
        }

        AnnotationInstance type = TypeUtil.getAnnotation(paramTarget, DOTNAME_RESTEASY_PART_TYPE);

        if (type != null) {
            Encoding encoding = new EncodingImpl();
            encoding.setContentType(type.value().asString());
            encodings.put(paramName, encoding);
        }
    }

    /**
     * Determine if this is an ignored parameter, per the MP+OAI specification in
     * {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter @Parameter}.
     *
     * @param parameter
     *        the parameter to determine if ignored
     * @return true if the parameter should be ignored, false otherwise
     *
     * @see org.eclipse.microprofile.openapi.annotations.parameters.Parameter#name()
     * @see org.eclipse.microprofile.openapi.annotations.parameters.Parameter#in()
     */
    static boolean isIgnoredParameter(ParameterImpl parameter) {
        String paramName = parameter.getName();
        In paramIn = parameter.getIn();

        if (paramIn == null) {
            /*
             * Per @Parameter JavaDoc, ignored when empty string (i.e., unspecified).
             * This may occur when @Parameter is specified without a matching JAX-RS
             * parameter annotation.
             */
            return true;
        }

        if (parameter.isHidden()) {
            return true;
        }

        /*
         * Name is REQUIRED unless it is a reference.
         */
        if ((paramName == null || paramName.trim().isEmpty()) && parameter.getRef() == null) {
            return true;
        }

        if (paramIn == Parameter.In.HEADER && paramName != null) {
            switch (paramName.toUpperCase()) {
                case "ACCEPT":
                case "AUTHORIZATION":
                case "CONTENT-TYPE":
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * Read a single annotation that is either {@link @Parameter} or
     * {@link @Parameters}. The results are stored in the private {@link #params}
     * collection.
     *
     * @param annotation a parameter annotation to be read and processed
     */
    void readParameterAnnotation(AnnotationInstance annotation) {
        DotName name = annotation.name();

        if (DOTNAME_PARAMETER.equals(name)) {
            readAnnotatedType(annotation, null);
        } else if (DOTNAME_PARAMETERS.equals(name)) {
            AnnotationValue annotationValue = annotation.value();

            if (annotationValue != null) {
                /*
                 * Unwrap annotations wrapped by @Parameters and
                 * identify the target as the target of the @Parameters annotation
                 */
                for (AnnotationInstance nested : annotationValue.asNestedArray()) {
                    readAnnotatedType(AnnotationInstance.create(nested.name(),
                            annotation.target(),
                            nested.values()),
                            null);
                }
            }
        }
    }

    /**
     * Read a single annotation that is either {@link @Parameter} or
     * one of the JAX-RS *Param annotations. The results are stored in the
     * private {@link #params} collection, depending on the type of parameter.
     *
     * @param annotation a parameter annotation to be read and processed
     */
    void readAnnotatedType(AnnotationInstance annotation) {
        readAnnotatedType(annotation, null);
    }

    /**
     * Read a single annotation that is either {@link @Parameter} or
     * one of the JAX-RS *Param annotations. The results are stored in the
     * private {@link #params} collection.
     *
     * @param annotation a parameter annotation to be read and processed
     * @param beanParamAnnotation
     */
    void readAnnotatedType(AnnotationInstance annotation, AnnotationInstance beanParamAnnotation) {
        DotName name = annotation.name();

        if (DOTNAME_PARAMETER.equals(name)) {
            ParameterImpl oaiParam = reader.apply(annotation);

            readParameter(oaiParam.getName(),
                    oaiParam.getIn(),
                    oaiParam,
                    null,
                    null,
                    annotation.target());
        } else {
            JaxRsParameter jaxRsParam = JaxRsParameter.forName(name);

            if (jaxRsParam != null) {
                AnnotationTarget target = annotation.target();

                if (jaxRsParam.style == Style.FORM) {
                    // Store the @FormParam for later processing
                    formParams.put(paramName(annotation), annotation);
                } else if (jaxRsParam.style == Style.MATRIX) {
                    // Store the @MatrixParam for later processing
                    String pathSegment = beanParamAnnotation != null
                            ? fullPathOf(beanParamAnnotation.target())
                            : fullPathOf(target);

                    if (!matrixParams.containsKey(pathSegment)) {
                        matrixParams.put(pathSegment, new HashMap<>());
                    }

                    matrixParams.get(pathSegment).put(paramName(annotation), annotation);
                } else if (jaxRsParam.location != null) {
                    readParameter(paramName(annotation),
                            jaxRsParam.location,
                            null,
                            jaxRsParam,
                            getDefaultValue(target),
                            target);
                } else if (target != null) {
                    // This is a @BeanParam or a RESTEasy @MultipartForm
                    DotName targetName = null;
                    setMediaType(jaxRsParam);

                    switch (target.kind()) {
                        case FIELD:
                            targetName = target.asField().type().name();
                            break;
                        case METHOD:
                            List<Type> methodParams = target.asMethod().parameters();
                            if (methodParams.size() == 1) {
                                // This is a bean property setter
                                targetName = methodParams.get(0).name();
                            }
                            break;
                        case METHOD_PARAMETER:
                            targetName = getMethodParameterType(target.asMethodParameter()).name();
                            break;
                        default:
                            break;
                    }

                    if (targetName != null) {
                        ClassInfo beanParam = index.getClassByName(targetName);
                        readParameters(beanParam, annotation);
                    }
                }
            }
        }
    }

    /**
     * Set this {@link ParameterProcessor}'s formMediaType if it has not already
     * been set and the value is explicitly known for the parameter type.
     *
     * @param jaxRsParam parameter to check for a form media type
     *
     */
    private void setMediaType(JaxRsParameter jaxRsParam) {
        if (jaxRsParam.mediaType != null && this.formMediaType == null) {
            formMediaType = jaxRsParam.mediaType;
        }
    }

    /**
     * Retrieves the "value" parameter from annotation to be used as the name.
     * If no value was specified or an empty value, return the name of the annotation
     * target.
     *
     * @param annotation parameter annotation
     * @return the name of the parameter
     */
    static String paramName(AnnotationInstance annotation) {
        AnnotationValue value = annotation.value();
        String valueString = null;

        if (value != null) {
            valueString = value.asString();
            if (valueString.length() > 0) {
                return valueString;
            }
        }

        AnnotationTarget target = annotation.target();

        switch (target.kind()) {
            case FIELD:
                valueString = target.asField().name();
                break;
            case METHOD_PARAMETER:
                valueString = target.asMethodParameter().name();
                break;
            case METHOD:
                // This is a bean property setter
                MethodInfo method = target.asMethod();
                if (method.parameters().size() == 1) {
                    String methodName = method.name();

                    if (methodName.startsWith("set")) {
                        valueString = Introspector.decapitalize(methodName.substring(3));
                    } else {
                        valueString = methodName;
                    }
                }
                break;
            default:
                break;
        }

        return valueString;
    }

    /**
     * Scan and parse a JAX-RS {@link javax.ws.rs.DefaultValue DefaultValue} annotation.
     * If the target is a Java primitive, the value will be parsed into an equivalent
     * wrapper object.
     *
     * @param target target annotated with {@link javax.ws.rs.DefaultValue @DefaultValue}
     * @return the default value
     */
    static Object getDefaultValue(AnnotationTarget target) {
        AnnotationInstance defaultValueAnno = TypeUtil.getAnnotation(target, DOTNAME_DEFAULT_VALUE);
        Object defaultValue = null;

        if (defaultValueAnno != null) {
            String defaultValueString = stringValue(defaultValueAnno, PROP_VALUE);
            defaultValue = defaultValueString;
            Type targetType = getType(target);

            if (targetType != null && targetType.kind() == Type.Kind.PRIMITIVE) {
                Primitive primitive = targetType.asPrimitiveType().primitive();

                try {
                    switch (primitive) {
                        case BOOLEAN:
                            defaultValue = Boolean.parseBoolean(defaultValueString);
                            break;
                        case CHAR:
                            if (defaultValueString.length() == 1) {
                                defaultValue = defaultValueString.charAt(0);
                            }
                            break;
                        case BYTE:
                            byte[] bytes = defaultValueString.getBytes();
                            if (bytes.length == 1) {
                                defaultValue = bytes[0];
                            }
                            break;
                        case SHORT:
                        case INT:
                        case LONG:
                            defaultValue = Long.valueOf(defaultValueString);
                            break;
                        case FLOAT:
                        case DOUBLE:
                            defaultValue = Double.valueOf(defaultValueString);
                            break;
                    }
                } catch (@SuppressWarnings("unused") Exception e) {
                    LOG.warnf("Value '%s' is not a valid %s default", defaultValueString, primitive.name().toLowerCase());
                }
            }
        }

        return defaultValue;
    }

    /**
     * Find the full path of the target. Method-level targets will include
     * both the path to the resource and the path to the method joined with a '/'.
     *
     * @param target target item for which the path is being generated
     * @return full path (excluding application path) of the target
     */
    static String fullPathOf(AnnotationTarget target) {
        String pathSegment = null;

        switch (target.kind()) {
            case FIELD:
                pathSegment = pathOf(target.asField().declaringClass());
                break;
            case METHOD:
                pathSegment = methodPath(target.asMethod());
                break;
            case METHOD_PARAMETER:
                pathSegment = methodPath(target.asMethodParameter().method());
                break;
            default:
                break;
        }

        return pathSegment;
    }

    /**
     * Concatenate the method's path with the path of its declaring
     * class.
     *
     * @param method the method annotated with {@link javax.ws.rs.Path Path}
     */
    static String methodPath(MethodInfo method) {
        String methodPath = pathOf(method);
        String classPath = pathOf(method.declaringClass());

        if (methodPath.isEmpty()) {
            return classPath;
        }

        return classPath + '/' + methodPath;
    }

    /**
     * Reads the {@link javax.ws.rs.Path @Path} annotation present on the
     * target and strips leading and trailing slashes.
     *
     * @param target target object
     * @return value of the {@link javax.ws.rs.Path @Path} without
     *         leading/trailing slashes.
     */
    static String pathOf(AnnotationTarget target) {
        AnnotationInstance path = null;

        switch (target.kind()) {
            case CLASS:
                path = target.asClass().classAnnotation(DOTNAME_PATH);
                break;
            case METHOD:
                path = target.asMethod().annotation(DOTNAME_PATH);
                break;
            default:
                break;
        }

        if (path != null) {
            String pathValue = path.value().asString();

            if (pathValue.startsWith("/")) {
                pathValue = pathValue.substring(1);
            }

            if (pathValue.endsWith("/")) {
                pathValue = pathValue.substring(0, pathValue.length() - 1);
            }

            return pathValue;
        }

        return "";
    }

    /**
     * Determines the type of the target. Method annotations will give
     * the name of a single argument, assumed to be a "setter" method.
     *
     * @param target target object
     * @return object type
     */
    static Type getType(AnnotationTarget target) {
        if (target == null) {
            return null;
        }

        Type type = null;

        switch (target.kind()) {
            case FIELD:
                type = target.asField().type();
                break;
            case METHOD:
                List<Type> methodParams = target.asMethod().parameters();
                if (methodParams.size() == 1) {
                    // This is a bean property setter
                    type = methodParams.get(0);
                }
                break;
            case METHOD_PARAMETER:
                type = getMethodParameterType(target.asMethodParameter());
                break;
            default:
                break;
        }

        return type;
    }

    /**
     * Merges MP-OAI {@link Parameter}s and JAX-RS parameters for the same {@link In} and name.
     *
     * @param name name of the parameter specified by application
     * @param location location, given by
     *        {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter#in @Parameter.in}
     *        or implied by the type of JAX-RS annotation used on the target
     * @param oaiParam scanned {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter @Parameter}
     * @param jaxRsParam Meta detail about the JAX-RS *Param being processed, if found.
     * @param jaxRsDefaultValue value read from the {@link javax.ws.rs.DefaultValue @DefaultValue}
     *        annotation.
     * @param target target of the annotation
     */
    void readParameter(String name,
            In location,
            ParameterImpl oaiParam,
            JaxRsParameter jaxRsParam,
            Object jaxRsDefaultValue,
            AnnotationTarget target) {

        //TODO: Test to ensure @Parameter attributes override JAX-RS for the same parameter
        //      (unless @Parameter was already specified at a "lower" level)

        ParameterContext context = getParameterContext(name, location, target);
        boolean addParam = false;

        if (context == null) {
            context = new ParameterContext();
            addParam = true;
        }

        if (oaiParam != null && name != null) {
            if (context.name != null) {
                // Name is being overridden by the OAI @Parameter name
                params.remove(new ParameterContextKey(context.name, context.location));
                addParam = true;
            }
            context.name = name;
        } else if (context.name == null) {
            // Name has not yet been set
            context.name = name;
        }

        if (context.location == null) {
            context.location = location;
        }

        context.oaiParam = MergeUtil.mergeObjects(context.oaiParam, oaiParam);

        if (context.jaxRsParam == null) {
            context.jaxRsParam = jaxRsParam;
            context.jaxRsDefaultValue = jaxRsDefaultValue;
        }

        if (context.target == null || context.target.kind() == Kind.METHOD) {
            context.target = target;
            context.targetType = getType(target);
        }

        if (addParam) {
            params.put(new ParameterContextKey(name, location), context);
        }
    }

    /**
     * Find a previously-created {@link ParameterContext} for the name and {@link In}
     *
     * If no match, check for a match using the annotation target. Finally, check for
     * a match on name alone.
     *
     * @param name parameter name
     * @param location {@link In} location of the parameter
     * @param target annotation target being processed
     * @return previously-create {@link ParameterContext} or null, if none found.
     */
    ParameterContext getParameterContext(String name,
            In location,
            AnnotationTarget target) {

        ParameterContext context = params.get(new ParameterContextKey(name, location));

        if (context == null && target.kind() != Kind.METHOD) {
            /*
             * If the annotations have the same (non-method) target, it's the same parameter.
             * This covers the situation where a @Parameter annotation was missing either
             * 'name' or 'location' attributes (or both).
             */
            context = params.values().stream().filter(c -> target.equals(c.target)).findFirst().orElse(null);
        }

        if (context == null) {
            /*
             * Allow a match on just the name if one of the Parameter.In values
             * is not specified
             */
            context = params.values().stream().filter(c -> {
                if (c.location == null || location == null) {
                    return c.name != null && c.name.equals(name);
                }
                return false;
            }).findFirst().orElse(null);

        }

        return context;
    }

    /**
     * Scans for class level parameters. This method is used for both resource class
     * annotation scanning and {@link javax.ws.rs.BeanParam @BeanParam} target type scanning.
     *
     * @param clazz the class to be scanned for parameters.
     * @param beanParamAnnotation
     */
    void readParameters(ClassInfo clazz, AnnotationInstance beanParamAnnotation) {
        for (Entry<DotName, List<AnnotationInstance>> entry : clazz.annotations().entrySet()) {
            DotName name = entry.getKey();

            if (DOTNAME_PARAMETER.equals(name) || JaxRsParameter.isParameter(name)) {
                for (AnnotationInstance annotation : entry.getValue()) {
                    if (isBeanPropertyParam(annotation)) {
                        readAnnotatedType(annotation, beanParamAnnotation);
                    }
                }
            }
        }
    }

    /**
     * Determines if the annotation is a property parameter. Annotation targets
     * must be annotated with a JAX-RS parameter annotation or
     * {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter @Parameter}.
     *
     * Method targets must not be annotated with one of the JAX-RS HTTP method annotations and
     * the method must have a single argument.
     *
     * @param annotation
     * @return
     */
    boolean isBeanPropertyParam(AnnotationInstance annotation) {
        AnnotationTarget target = annotation.target();
        boolean relevant = false;

        switch (target.kind()) {
            case FIELD:
                FieldInfo field = target.asField();
                relevant = hasParameters(field.annotations());
                break;
            case METHOD_PARAMETER:
                MethodParameterInfo param = target.asMethodParameter();
                relevant = !isResourceMethod(param.method()) &&
                        hasParameters(TypeUtil.getAnnotations(param)) &&
                        !isSubResourceLocator(param.method());
                break;
            case METHOD:
                MethodInfo method = target.asMethod();
                relevant = !isResourceMethod(method) &&
                        hasParameters(method.annotations()) &&
                        getType(target) != null &&
                        !isSubResourceLocator(method);
                break;
            default:
                break;
        }

        return relevant;
    }

    /**
     * Determines if the given method is a JAX-RS sub-resource locator method
     * annotated by {@code @Path} but NOT annotated with one of the HTTP method
     * annotations.
     *
     * @param method method to check
     * @return true if the method is JAX-RS sub-resource locator, false otherwise
     */
    boolean isSubResourceLocator(MethodInfo method) {
        return method.returnType().kind() == Type.Kind.CLASS &&
                method.hasAnnotation(DOTNAME_PATH) &&
                method.annotations().stream()
                        .map(AnnotationInstance::name)
                        .noneMatch(DOTNAME_JAXRS_HTTP_METHODS::contains);
    }

    /**
     * Determines if the given method is a JAX-RS resource method annotated by one
     * of the HTTP method annotations.
     *
     * @param method method to check
     * @return true if the method is annotated with a JAX-RS HTTP method annotation, false otherwise
     */
    static boolean isResourceMethod(MethodInfo method) {
        return method.annotations()
                .stream()
                .map(AnnotationInstance::name)
                .anyMatch(DOTNAME_JAXRS_HTTP_METHODS::contains);
    }

    /**
     * Check for the existence relevant parameter annotations in the collection.
     *
     * @param annotations collection of annotations
     * @return true if any of the annotations is a relevant parameter annotation.
     */
    static boolean hasParameters(Collection<AnnotationInstance> annotations) {
        return annotations.stream()
                .map(AnnotationInstance::name)
                .anyMatch(ParameterProcessor::isParameter);
    }

    static boolean isParameter(DotName annotationName) {
        if (JaxRsParameter.isParameter(annotationName)) {
            return true;
        }
        if (DOTNAME_PARAMETER.equals(annotationName)) {
            return true;
        }
        return DOTNAME_PARAMETERS.equals(annotationName);
    }
}
