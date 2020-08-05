package io.smallrye.openapi.vertx;

import static io.smallrye.openapi.api.constants.JDKConstants.DOTNAME_DEPRECATED;
import static io.smallrye.openapi.api.util.MergeUtil.mergeObjects;
import static io.smallrye.openapi.runtime.util.JandexUtil.getMethodParameterType;
import static org.jboss.jandex.AnnotationTarget.Kind.CLASS;
import static org.jboss.jandex.AnnotationTarget.Kind.METHOD;

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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
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

import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.parameter.ParameterConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.dataobject.BeanValidationScanner;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Copied from JAX-RS. Still need clean up
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ParameterProcessor {

    /**
     * Sonar validation is disabled on this expression because there is no danger of denial of
     * service attacks input derived from the developer of the host application.
     */
    static final Pattern TEMPLATE_PARAM_PATTERN = Pattern
            .compile(":[ \\t]*(\\w[\\w\\.-]*)[ \\t]*:[ \\t]*((?:[^{}]|\\{[^{}]+\\})+)"); //NOSONAR

    private static Set<DotName> openApiParameterAnnotations = new HashSet<>(
            Arrays.asList(ParameterConstant.DOTNAME_PARAMETER, ParameterConstant.DOTNAME_PARAMETERS));

    private final IndexView index;
    private final ClassLoader cl;
    private final Function<AnnotationInstance, Parameter> readerFunction;
    private final List<AnnotationScannerExtension> extensions;

    /**
     * Collection of parameters scanned at the current level. This map contains
     * all parameter types except for form parameters and Vert.x MatrixParam.
     */
    private Map<ParameterContextKey, ParameterContext> params = new HashMap<>();

    /**
     * Collection of Vert.x FormParam found during scanning.
     * Does Vert.x have this ?
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
     * Collection of Vert.x MatrixParam found during scanning.
     * These annotations will be used as schema properties for the schema of a path parameter
     * having {@link Parameter#setStyle style} of {@link Style#MATRIX}.
     */
    private Map<String, Map<String, AnnotationInstance>> matrixParams = new LinkedHashMap<>();

    private Set<String> processedMatrixSegments = new HashSet<>();

    /**
     * Used for collecting and merging any scanned {@link Parameter} annotations
     * with the Vert.x Mapping annotations. After scanning, this object may
     * contain either the MP-OAI annotation information, the Vert.x annotation
     * information, or both.
     *
     * @author Michael Edgar {@literal <michael@xlate.io>}
     */
    static class ParameterContext {
        String name;
        In location;
        Style style;
        Parameter oaiParam;
        VertxParameter vertxParam;
        Object vertxDefaultValue;
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
        final String name;
        final In location;
        final Style style;

        ParameterContextKey(String name, In location, Style style) {
            this.name = name;
            this.location = location;
            this.style = style;
        }

        ParameterContextKey(ParameterContext context) {
            this.name = context.name;
            this.location = context.location;
            this.style = context.style;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ParameterContextKey) {
                ParameterContextKey other = (ParameterContextKey) obj;

                return Objects.equals(this.name, other.name) &&
                        Objects.equals(this.location, other.location) &&
                        Objects.equals(this.style, other.style);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, location, style);
        }

        @Override
        public String toString() {
            return "name: " + name + "; in: " + location;
        }
    }

    private ParameterProcessor(IndexView index,
            ClassLoader cl,
            Function<AnnotationInstance, Parameter> reader,
            List<AnnotationScannerExtension> extensions) {
        this.index = index;
        this.cl = cl;
        this.readerFunction = reader;
        this.extensions = extensions;
    }

    /**
     * Process parameter annotations for the given class and method.This method operates
     * in two phases. First, class-level parameters are processed and saved in the
     * {@link ResourceParameters}. Second, method-level parameters are processed. Form parameters
     * are only applicable to the method-level in this component.
     *
     * @param index index of classes to be used for further introspection, if necessary
     * @param resourceClass the class info
     * @param resourceMethod the Vert.x resource method, annotated with one of the
     *        Vert.x HTTP annotations
     * @param reader callback method for a function producing {@link Parameter} from a
     *        {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter}
     * @param extensions scanner extensions
     * @return scanned parameters and modified path contained in a {@link ResourceParameters}
     *         object
     */
    public static ResourceParameters process(IndexView index,
            ClassLoader cl,
            ClassInfo resourceClass,
            MethodInfo resourceMethod,
            Function<AnnotationInstance, Parameter> reader,
            List<AnnotationScannerExtension> extensions) {

        ResourceParameters parameters = new ResourceParameters();
        ParameterProcessor processor = new ParameterProcessor(index, cl, reader, extensions);

        // MP Open API Parameters
        parameters.setPathItemParameters(processor.getParameters(resourceMethod));

        // Clear Path-level parameters discovered and allows for processing operation-level parameters
        processor.reset();

        // Read method argument @Parameter and Vert Mapping annotations
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
                        processor.readParameterAnnotation(annotation, resourceMethod);
                    } else {
                        processor.readAnnotatedType(annotation, resourceMethod);
                    }
                });

        // Phase III - Read method @Parameter(s) annotations
        resourceMethod.annotations()
                .stream()
                .filter(a -> a.target().equals(resourceMethod))
                .filter(a -> openApiParameterAnnotations.contains(a.name()))
                .forEach(t -> processor.readParameterAnnotation(t, resourceMethod));

        parameters.setOperationParameters(processor.getParameters(resourceMethod));

        /*
         * Working list of all parameters.
         */
        List<Parameter> allParameters = parameters.getAllParameters();
        /*
         * Generate the path using the provided resource class, which may differ from the method's declaring
         * class - e.g. for inheritance.
         */
        parameters.setPathItemPath(processor.generatePath(resourceClass, allParameters));
        parameters.setOperationPath(processor.generatePath(resourceMethod, allParameters));

        // Re-sort (names of matrix parameters may have changed)
        parameters.sort();

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
    String generatePath(AnnotationTarget target, List<Parameter> parameters) {
        final StringBuilder path = new StringBuilder(pathOf(target));

        if (path.length() > 0) {
            path.insert(0, '/');
        }

        /*
         * Search for path template variables where a regular expression
         * is specified, extract the pattern and apply to the parameter's schema
         * if no pattern is otherwise specified and the parameter is a string.
         */
        Matcher templateMatcher = TEMPLATE_PARAM_PATTERN.matcher(path);

        while (templateMatcher.find()) {
            String variableName = templateMatcher.group(1).trim();
            String variablePattern = templateMatcher.group(2).trim();

            parameters.stream()
                    .filter(p -> variableName.equals(p.getName()))
                    .filter(ParameterProcessor::templateParameterPatternEligible)
                    .forEach(p -> p.getSchema().setPattern(variablePattern));

            String replacement = templateMatcher.replaceFirst('{' + variableName + '}');
            path.setLength(0);
            path.append(replacement);

            templateMatcher = TEMPLATE_PARAM_PATTERN.matcher(path);
        }

        parameters.stream()
                .filter(p -> Style.MATRIX.equals(p.getStyle()))
                .filter(p -> !processedMatrixSegments.contains(p.getName()))
                .filter(p -> path.indexOf(p.getName()) > -1)
                .forEach(matrix -> {
                    String segmentName = matrix.getName();
                    processedMatrixSegments.add(segmentName);

                    String matrixRef = '{' + segmentName + '}';
                    int insertIndex = -1;

                    if ((insertIndex = path.lastIndexOf(matrixRef)) > -1) {
                        insertIndex += matrixRef.length();
                        // Path already contains a variable of same name, the matrix must be renamed
                        String generatedName = segmentName + "Matrix";
                        matrix.setName(generatedName);
                        matrixRef = '{' + generatedName + '}';
                    } else if ((insertIndex = path.lastIndexOf(segmentName)) > -1) {
                        insertIndex += segmentName.length();
                    }

                    if (insertIndex > -1) {
                        path.insert(insertIndex, matrixRef);
                    } else {
                        VertxLogging.log.missingPathSegment(segmentName);
                    }
                });

        return path.toString();
    }

    /**
     * Determines if the parameter is eligible to have a pattern constraint
     * applied to its schema.
     *
     * @param param the parameter
     * @return true if the parameter may have the patter applied, otherwise false
     */
    static boolean templateParameterPatternEligible(Parameter param) {
        return Parameter.In.PATH.equals(param.getIn())
                && !Style.MATRIX.equals(param.getStyle())
                && param.getSchema() != null
                && SchemaType.STRING.equals(param.getSchema().getType())
                && param.getSchema().getPattern() == null;
    }

    /**
     * Performs the final merging of Vert.x parameters with MP-OAI parameters to produce the list
     * of {@link Parameter}s found while scanning the current level (class or method).
     *
     * @return list of {@link Parameter}s
     */
    private List<Parameter> getParameters(MethodInfo resourceMethod) {
        List<Parameter> parameters = new ArrayList<>();

        // Process any Matrix Parameters found
        for (Entry<String, Map<String, AnnotationInstance>> matrixPath : matrixParams.entrySet()) {
            String segmentName = matrixPath.getKey();

            //Find a ParamContext style=matrix at this path
            ParameterContext context = params.values()
                    .stream()
                    .filter(p -> p.oaiParam != null)
                    .filter(p -> p.oaiParam.getStyle() == Style.MATRIX)
                    .filter(p -> segmentName.equals(p.name))
                    .findFirst()
                    .orElseGet(() -> {
                        /*
                         * @Parameter style=matrix was not specified at the same @Path segment of
                         * this @MatrixParam. Generate one here
                         */
                        ParameterContext generated = new ParameterContext();
                        generated.name = segmentName;
                        generated.location = In.PATH;
                        generated.style = Style.MATRIX;
                        generated.target = null;
                        generated.targetType = null;
                        generated.oaiParam = new ParameterImpl();
                        generated.oaiParam.setStyle(Style.MATRIX);
                        generated.oaiParam.setExplode(Boolean.TRUE);
                        params.put(new ParameterContextKey(generated), generated);
                        return generated;
                    });

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

        // Convert ParameterContext entries to MP-OAI Parameters
        params.values().stream().forEach(context -> {
            Parameter param;

            if (context.oaiParam == null) {
                param = new ParameterImpl();
            } else {
                param = context.oaiParam;
            }

            param.setName(context.name);

            if (param.getIn() == null && context.location != null) {
                param.setIn(context.location);
            }

            if (isIgnoredParameter(param, resourceMethod)) {
                return;
            }

            if (param.getIn() == In.PATH) {
                param.setRequired(true);
            }

            if (param.getStyle() == null && context.vertxParam != null) {
                param.setStyle(context.vertxParam.style);
            }

            if (!ModelUtil.parameterHasSchema(param) && context.targetType != null) {
                Schema schema = SchemaFactory.typeToSchema(index, cl, context.targetType, extensions);
                ModelUtil.setParameterSchema(param, schema);
            }

            if (param.getDeprecated() == null && TypeUtil.hasAnnotation(context.target, DOTNAME_DEPRECATED)) {
                param.setDeprecated(Boolean.TRUE);
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
                    param.getSchema().setDefaultValue(context.vertxDefaultValue);
                }
            }

            if (param.getRequired() == null && TypeUtil.isOptional(context.targetType)) {
                param.setRequired(Boolean.FALSE);
            }

            parameters.add(param);
        });

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
     * 'application/x-www-form-urlencoded'
     *
     * @return generated form content
     */
    private Content getFormBodyContent() {
        if (formParams.isEmpty()) {
            return null;
        }

        Content content = new ContentImpl();
        MediaType mediaType = new MediaTypeImpl();
        Schema schema = new SchemaImpl();
        Map<String, Encoding> encodings = new HashMap<>();
        schema.setType(SchemaType.OBJECT);

        mediaType.setSchema(schema);
        setSchemaProperties(schema, encodings, formParams);

        if (encodings.size() > 0) {
            mediaType.setEncoding(encodings);
        }

        // TODO: Do this for Vert.x ?
        //String mediaTypeName = formMediaType != null ? formMediaType : APPLICATION_FORM_URLENCODED;
        //content.addMediaType(mediaTypeName, mediaType);

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

            Type paramType = getType(paramTarget);
            Schema paramSchema = SchemaFactory.typeToSchema(index, cl, paramType, extensions);

            BeanValidationScanner.applyConstraints(paramTarget,
                    paramSchema,
                    paramName,
                    (target, name) -> {
                        List<String> requiredProperties = schema.getRequired();

                        if (requiredProperties == null || !requiredProperties.contains(name)) {
                            schema.addRequired(name);
                        }
                    });

            if (paramSchema.getNullable() == null && TypeUtil.isOptional(paramType)) {
                paramSchema.setNullable(Boolean.TRUE);
            }

            if (schema.getProperties() != null) {
                paramSchema = mergeObjects(schema.getProperties().get(paramName), paramSchema);
            }
            schema.addProperty(paramName, paramSchema);
        }
    }

    /**
     * Determine if this is an ignored parameter, per the MP+OAI specification in
     * {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter @Parameter}.
     *
     * Path parameters that do not have a corresponding path segment will be ignored.
     *
     * @param parameter
     *        the parameter to determine if ignored
     * @param resourceMethod
     *        the resource method to which the parameter may apply
     * @return true if the parameter should be ignored, false otherwise
     *
     * @see org.eclipse.microprofile.openapi.annotations.parameters.Parameter#name()
     * @see org.eclipse.microprofile.openapi.annotations.parameters.Parameter#in()
     */
    static boolean isIgnoredParameter(Parameter parameter, AnnotationTarget resourceMethod) {
        String paramName = parameter.getName();
        In paramIn = parameter.getIn();

        if (paramIn == null) {
            /*
             * Per @Parameter JavaDoc, ignored when empty string (i.e., unspecified).
             * This may occur when @Parameter is specified without a matching Vert.x
             * parameter annotation.
             */
            return true;
        }

        if (ParameterImpl.isHidden(parameter)) {
            return true;
        }

        /*
         * Name is REQUIRED unless it is a reference.
         */
        if ((paramName == null || paramName.trim().isEmpty()) && parameter.getRef() == null) {
            return true;
        }

        if (paramIn == In.PATH && !parameterInPath(paramName, parameter.getStyle(), fullPathOf(resourceMethod))) {
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
     * Check if the given parameter name is present as a path segment in the resourcePath.
     *
     * @param paramName name of parameter
     * @param paramStyle style of parameter, e.g. simple or matrix
     * @param resourcePath resource path/URL
     * @return true if the paramName is in the resourcePath, false otherwise.
     */
    static boolean parameterInPath(String paramName, Style paramStyle, String resourcePath) {
        if (paramName == null || resourcePath == null) {
            return true;
        }

        final String regex;

        if (Style.MATRIX.equals(paramStyle)) {
            regex = String.format("(?:\\{[ \\t]*|^|/?)\\Q%s\\E(?:[ \\t]*(?:}|:)|/?|$)", paramName); // TODO: How does apply in Vertx context ?
        } else {
            regex = String.format("\\{[ \\t]*\\Q%s\\E[ \\t]*(?:}|:)", paramName);
        }

        return Pattern.compile(regex).matcher(resourcePath).find();
    }

    /**
     * Read a single annotation that is either {@link @Parameter} or
     * {@link @Parameters}. The results are stored in the private {@link #params}
     * collection.
     *
     * @param annotation a parameter annotation to be read and processed
     */
    void readParameterAnnotation(AnnotationInstance annotation, MethodInfo resourceMethod) {
        DotName name = annotation.name();

        if (ParameterConstant.DOTNAME_PARAMETER.equals(name)) {
            readAnnotatedType(annotation, resourceMethod, null, false);
        } else if (ParameterConstant.DOTNAME_PARAMETERS.equals(name)) {
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
                            resourceMethod,
                            null,
                            false);
                }
            }
        }
    }

    /**
     * Read a single annotation that is either {@link @Parameter} or
     * one of the Vert.x Mapping annotations. The results are stored in the
     * private {@link #params} collection, depending on the type of parameter.
     *
     * @param annotation a parameter annotation to be read and processed
     */
    void readAnnotatedType(AnnotationInstance annotation, MethodInfo resourceMethod) {
        readAnnotatedType(annotation, resourceMethod, null, false);
    }

    /**
     * Read a single annotation that is either {@link @Parameter} or
     * one of the Vert.x Mapping annotations. The results are stored in the
     * private {@link #params} collection. When overriddenParametersOnly is true,
     * new parameters not already known in {@link #params} will be ignored.
     *
     * @param annotation a parameter annotation to be read and processed
     * @param beanParamAnnotation
     * @param overriddenParametersOnly
     */
    void readAnnotatedType(AnnotationInstance annotation, MethodInfo resourceMethod, AnnotationInstance beanParamAnnotation,
            boolean overriddenParametersOnly) {

        DotName name = annotation.name();

        AnnotationInstance routeAnnotation = resourceMethod.annotation(VertxConstants.ROUTE);
        AnnotationValue pathValue = routeAnnotation.value("path");
        String path = resourceMethod.name(); // default to methodName
        if (pathValue != null) {
            path = pathValue.asString();
        }

        if (ParameterConstant.DOTNAME_PARAMETER.equals(name) && readerFunction != null) {
            Parameter oaiParam = readerFunction.apply(annotation);
            readParameter(new ParameterContextKey(oaiParam.getName(), oaiParam.getIn(), styleOf(oaiParam)),
                    oaiParam,
                    null,
                    annotation.target(),
                    overriddenParametersOnly);
        } else if (VertxConstants.PARAM.equals(name) && annotation.value() != null) {
            String parameterName = annotation.value().asString();
            if (path.contains(":" + parameterName)) {
                VertxParameter vertxParameter = VertxParameter.PATH_PARAM;
                readAnnotatedType(vertxParameter, annotation, resourceMethod, beanParamAnnotation, overriddenParametersOnly);
            } else {
                VertxParameter vertxParameter = VertxParameter.QUERY_PARAM;
                readAnnotatedType(vertxParameter, annotation, resourceMethod, beanParamAnnotation, overriddenParametersOnly);
            }

        } else if (VertxConstants.HEADER_PARAM.equals(name) && annotation.value() != null) {
            VertxParameter vertxParameter = VertxParameter.HEADER_PARAM;
            readAnnotatedType(vertxParameter, annotation, resourceMethod, beanParamAnnotation, overriddenParametersOnly);
        }

    }

    private void readAnnotatedType(VertxParameter vertxParameter, AnnotationInstance annotation, MethodInfo resourceMethod,
            AnnotationInstance beanParamAnnotation, boolean overriddenParametersOnly) {
        if (vertxParameter != null) {
            AnnotationTarget target = annotation.target();
            Type targetType = getType(target);

            if (vertxParameter.style == Style.FORM) {
                // Store the @FormParam for later processing
                formParams.put(paramName(annotation), annotation);
            } else if (vertxParameter.style == Style.MATRIX) {
                // Store the @MatrixParam for later processing
                String pathSegment = beanParamAnnotation != null
                        ? lastPathSegmentOf(beanParamAnnotation.target())
                        : lastPathSegmentOf(target);

                if (!matrixParams.containsKey(pathSegment)) {
                    matrixParams.put(pathSegment, new HashMap<>());
                }

                matrixParams.get(pathSegment).put(paramName(annotation), annotation);
                // Do this in Vert.x ?
                //}else if (vertxParam.location == In.PATH && targetType != null
                //      && VertxConstants.REQUEST_MAPPING.equals(targetType.name())) {
                //    String pathSegment = JandexUtil.value(annotation, ParameterConstant.PROP_VALUE);

                //    if (!matrixParams.containsKey(pathSegment)) {
                //        matrixParams.put(pathSegment, new HashMap<>());
                //   }
            } else if (vertxParameter.location != null) {
                readParameter(
                        new ParameterContextKey(paramName(annotation), vertxParameter.location,
                                vertxParameter.defaultStyle),
                        null,
                        vertxParameter,
                        target,
                        overriddenParametersOnly);
            } else if (target != null) {
                // This is a @BeanParam or a RESTEasy @MultipartForm
                setMediaType(vertxParameter);

                if (TypeUtil.isOptional(targetType)) {
                    targetType = TypeUtil.getOptionalType(targetType);
                }

                if (targetType != null) {
                    ClassInfo beanParam = index.getClassByName(targetType.name());
                    readParameters(beanParam, resourceMethod, annotation, overriddenParametersOnly);
                }
            }
        }
    }

    /**
     * Retrieves either the provided parameter {@link Parameter.Style}, the default
     * style of the parameter based on the <code>in</code> attribute, or null if <code>in</code> is not defined.
     *
     * @param param the {@link Parameter}
     * @return the param's style, the default style defined based on <code>in</code>, or null if <code>in</code> is not defined.
     */
    Style styleOf(Parameter param) {
        if (param.getStyle() != null) {
            return param.getStyle();
        }

        if (param.getIn() != null) {
            switch (param.getIn()) {
                case COOKIE:
                case QUERY:
                    return Style.FORM;
                case HEADER:
                case PATH:
                    return Style.SIMPLE;
                default:
                    break;
            }
        }

        return null;
    }

    /**
     * Set this {@link ParameterProcessor}'s formMediaType if it has not already
     * been set and the value is explicitly known for the parameter type.
     *
     * @param vertxParam parameter to check for a form media type
     *
     */
    private void setMediaType(VertxParameter vertxParam) {
        if (vertxParam.mediaType != null && this.formMediaType == null) {
            formMediaType = vertxParam.mediaType;
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

    static Object primitiveToObject(Primitive primitive, String stringValue) {
        Object value = null;

        try {
            switch (primitive) {
                case BOOLEAN:
                    value = Boolean.parseBoolean(stringValue);
                    break;
                case CHAR:
                    if (stringValue.length() == 1) {
                        value = Character.valueOf(stringValue.charAt(0));
                    }
                    break;
                case BYTE:
                    byte[] bytes = stringValue.getBytes();
                    if (bytes.length == 1) {
                        value = Byte.valueOf(bytes[0]);
                    }
                    break;
                case SHORT:
                case INT:
                case LONG:
                    value = Long.valueOf(stringValue);
                    break;
                case FLOAT:
                case DOUBLE:
                    value = Double.valueOf(stringValue);
                    break;
            }
        } catch (@SuppressWarnings("unused") Exception e) {
            VertxLogging.log.invalidDefault(stringValue, primitive.name().toLowerCase());
        }

        return value;
    }

    /**
     * Retrieves the last path segment of the full path associated with the target. If
     * the last path segment contains a path variable name, returns the variable name.
     *
     * @param target
     * @return the last path segment of the target, or null if no path is defined
     */
    static String lastPathSegmentOf(AnnotationTarget target) {
        String fullPath = fullPathOf(target);
        String lastSegment = null;

        if (fullPath != null) {
            lastSegment = fullPath.substring(fullPath.lastIndexOf('/') + 1);

            if (lastSegment.startsWith("{") && lastSegment.endsWith("}")) {
                lastSegment = lastSegment.substring(1, lastSegment.length() - 1);
            }
        }

        return lastSegment;
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
     * @param method the method annotated with a Vert.x mapping
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
     * Reads the Vert.x mapping annotations present on the
     * target and strips leading and trailing slashes.
     *
     * @param target target object
     * @return value of the Vertx mapping annotation without
     *         leading/trailing slashes.
     */
    static String pathOf(AnnotationTarget target) {
        AnnotationInstance path = null;
        String defaultPathValue = null;
        if (target.kind().equals(CLASS)) {
            DotName possiblePath = VertxConstants.ROUTE_BASE;
            AnnotationInstance classAnnotation = target.asClass().classAnnotation(possiblePath);
            if (classAnnotation != null && classAnnotation.value("path") != null) {
                path = classAnnotation;
            }
        } else if (target.kind().equals(METHOD)) {
            defaultPathValue = target.asMethod().name();
            DotName possiblePath = VertxConstants.ROUTE;
            AnnotationInstance methodAnnotation = target.asMethod().annotation(possiblePath);
            if (methodAnnotation != null) {
                path = methodAnnotation;
            }
        }

        if (path != null) {
            String pathValue = routePathValuesToPath(path, defaultPathValue);
            if (pathValue != null) {
                if (pathValue.startsWith("/")) {
                    pathValue = pathValue.substring(1);
                }

                if (pathValue.endsWith("/")) {
                    pathValue = pathValue.substring(0, pathValue.length() - 1);
                }

                // Replace :var with {var}
                if (pathValue.contains(":")) {
                    List<String> parts = Arrays.asList(pathValue.split("/"));
                    List<String> partsConverted = new ArrayList<>();
                    for (String part : parts) {
                        if (part.startsWith(":")) {
                            part = "{" + part.substring(1) + "}";
                        }
                        partsConverted.add(part);
                    }
                    pathValue = String.join("/", partsConverted.toArray(new String[] {}));
                }
                return pathValue;
            }
        }

        return "";
    }

    /**
     * Creates a String path from the Route path value
     *
     * @param routeAnnotation
     * @return
     */
    static String routePathValuesToPath(AnnotationInstance routeAnnotation, String defaultValue) {
        AnnotationValue value = routeAnnotation.value("path");
        if (value != null) {
            return value.asString();
        }
        return defaultValue;
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
     * Merges MP-OAI {@link Parameter}s and Vert.x parameters for the same {@link In} and name,
     * and {@link Style}. When overriddenParametersOnly is true, new parameters not already known
     * in {@link #params} will be ignored.
     *
     * The given {@link ParameterContextKey key} contains:
     *
     * <ul>
     * <li>the name of the parameter specified by application
     * <li>location, given by {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter#in @Parameter.in}
     * or implied by the type of Vert.x annotation used on the target
     * <li>style, the parameter's style, either specified by the application or implied by the parameter's location
     * </ul>
     *
     * @param key the key for the parameter being processed
     * @param oaiParam scanned {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter @Parameter}
     * @param vertxParam Meta detail about the Vert.x route being processed, if found.
     * @param target target of the annotation
     * @param overriddenParametersOnly
     */
    void readParameter(ParameterContextKey key,
            Parameter oaiParam,
            VertxParameter vertxParam,
            AnnotationTarget target,
            boolean overriddenParametersOnly) {

        //TODO: Test to ensure @Parameter attributes override Vert.x for the same parameter
        //      (unless @Parameter was already specified at a "lower" level)

        ParameterContext context = getParameterContext(key, target);
        boolean addParam = false;

        if (context == null) {
            if (overriddenParametersOnly) {
                return;
            }

            context = new ParameterContext();
            addParam = true;
        }

        boolean oaiNameOverride = oaiParam != null && key.name != null && !key.name.equals(context.name)
                && context.location != In.PATH;

        if (context.name == null || oaiNameOverride) {
            if (context.name != null) {
                // Name is being overridden by the OAI @Parameter name
                params.remove(new ParameterContextKey(context));
                addParam = true;
            }
            context.name = key.name;
        }

        if (context.location == null) {
            context.location = key.location;
        }

        if (context.style == null) {
            context.style = key.style;
        }

        context.oaiParam = MergeUtil.mergeObjects(context.oaiParam, oaiParam);

        if (context.vertxParam == null) {
            context.vertxParam = vertxParam;
        }

        if (context.target == null || context.target.kind() == Kind.METHOD) {
            context.target = target;
            context.targetType = getType(target);
        }

        if (addParam) {
            params.put(new ParameterContextKey(context), context);
        }
    }

    /**
     * Find a previously-created {@link ParameterContext} for the given {@link ParameterContextKey key}.
     *
     * If no match, check for a match using the annotation target. Finally, check for
     * a match on name and style alone.
     *
     * @param key the key for the parameter being processed
     * @param target annotation target being processed
     * @return previously-create {@link ParameterContext} or null, if none found.
     */
    ParameterContext getParameterContext(ParameterContextKey key, AnnotationTarget target) {

        ParameterContext context = params.get(key);

        if (context == null) {
            context = params
                    .values()
                    .stream()
                    .filter(c -> haveSameAnnotatedTarget(c, target, key.name))
                    .findFirst()
                    .orElse(null);
        }

        if (context == null) {
            /*
             * Allow a match on just the name and style if one of the Parameter.In values
             * is not specified
             */
            context = params.values().stream().filter(c -> {
                if (c.location == null || key.location == null) {
                    return Objects.equals(c.name, key.name) && Objects.equals(c.style, key.style);
                }
                return false;
            }).findFirst().orElse(null);

        }

        return context;
    }

    boolean haveSameAnnotatedTarget(ParameterContext context, AnnotationTarget target, String name) {
        boolean nameMatches = Objects.equals(context.name, name);

        if (target.equals(context.target)) {
            return true;
        }

        if (nameMatches && target.kind() == Kind.METHOD && context.target.kind() == Kind.METHOD_PARAMETER) {
            return context.target.asMethodParameter().method().equals(target);
        }

        return false;
    }

    /**
     * Scans for class level parameters. This method is used for both resource class
     * annotation scanning and {@link javax.ws.rs.BeanParam @BeanParam} target type scanning.
     *
     * @param clazz the class to be scanned for parameters.
     * @param beanParamAnnotation
     */
    void readParameters(ClassInfo clazz, MethodInfo resourceMethod, AnnotationInstance beanParamAnnotation,
            boolean overriddenParametersOnly) {
        for (Entry<DotName, List<AnnotationInstance>> entry : clazz.annotations().entrySet()) {
            DotName name = entry.getKey();

            if (ParameterConstant.DOTNAME_PARAMETER.equals(name) || VertxParameter.isParameter(name)) {
                for (AnnotationInstance annotation : entry.getValue()) {
                    if (isBeanPropertyParam(annotation)) {
                        readAnnotatedType(annotation, resourceMethod, beanParamAnnotation, overriddenParametersOnly);
                    }
                }
            }
        }
    }

    /**
     * Determines if the annotation is a property parameter. Annotation targets
     * must be annotated with a Vert.x parameter annotation or
     * {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter @Parameter}.
     *
     * Method targets must not be annotated with one of the Vert.x HTTP method annotations and
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
     * Determines if the given method is a Vert.x sub-resource locator method
     * annotated by {@code @Path} but NOT annotated with one of the HTTP method
     * annotations.
     *
     * @param method method to check
     * @return true if the method is Vert.x sub-resource locator, false otherwise
     */
    boolean isSubResourceLocator(MethodInfo method) {
        //        return method.returnType().kind() == Type.Kind.CLASS &&
        //                isResourceMethod(method) &&
        //                method.annotations().stream()
        //                        .map(AnnotationInstance::name)
        //                        .noneMatch(VertxConstants.HTTP_METHODS::contains);
        return false;
    }

    /**
     * Determines if the given method is a Vert.x resource method annotated by one
     * of the HTTP method annotations.
     *
     * @param method method to check
     * @return true if the method is annotated with a Vert.x HTTP method annotation, false otherwise
     */
    static boolean isResourceMethod(MethodInfo method) {
        return method.annotations()
                .stream()
                .map(AnnotationInstance::name)
                .anyMatch(VertxConstants.ROUTE::equals);
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
        if (VertxParameter.isParameter(annotationName)) {
            return true;
        }
        if (ParameterConstant.DOTNAME_PARAMETER.equals(annotationName)) {
            return true;
        }
        return ParameterConstant.DOTNAME_PARAMETERS.equals(annotationName);
    }
}
