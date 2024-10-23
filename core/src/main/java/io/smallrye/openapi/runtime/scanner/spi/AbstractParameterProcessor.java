package io.smallrye.openapi.runtime.scanner.spi;

import static io.smallrye.openapi.api.util.MergeUtil.mergeObjects;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.OASFactory;
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

import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.model.Extensions;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;
import io.smallrye.openapi.runtime.scanner.dataobject.BeanValidationScanner;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 *
 * Common parameter processing that may be customized by individual frameworks
 * such as JAX-RS, Spring, Vert.x, etc.
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 *
 */
public abstract class AbstractParameterProcessor {

    private static Set<DotName> openApiParameterAnnotations = new HashSet<>(
            Arrays.asList(Names.PARAMETER, Names.PARAMETERS));

    protected static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    protected final AnnotationScannerContext scannerContext;
    protected final String contextPath;
    protected final IndexView index;
    protected final Function<AnnotationInstance, Parameter> readerFunction;
    protected final List<AnnotationScannerExtension> extensions;
    protected final Optional<BeanValidationScanner> beanValidationScanner;

    /**
     * Collection of parameters scanned at the current level. This map contains
     * all parameter types except for form parameters and matrix parameters.
     */
    protected Map<ParameterContextKey, ParameterContext> params = new HashMap<>();

    /**
     * Collection of form parameters found during scanning.
     * These annotations will be used as schema properties for a generated schema used in the
     * MP-OAI {@link org.eclipse.microprofile.openapi.models.responses.APIResponse APIResponse}
     * if a value has not be provided by the application.
     */
    protected Map<String, AnnotationInstance> formParams = new LinkedHashMap<>();

    /**
     * The media type of a form schema found while scanning the parameters.
     */
    protected String formMediaType;

    /**
     * Collection of matrix parameters found during scanning.
     * These annotations will be used as schema properties for the schema of a path parameter
     * having {@link Parameter#setStyle style} of {@link Style#MATRIX}.
     */
    protected Map<String, Map<String, AnnotationInstance>> matrixParams = new LinkedHashMap<>();

    private Set<String> processedMatrixSegments = new HashSet<>();
    private List<Parameter> preferredOrder;

    /**
     * Used for collecting and merging any scanned {@link Parameter} annotations
     * with the framework-specific parameter annotations. After scanning, this object may
     * contain either the MP-OAI annotation information, the framework's annotation
     * information, or both.
     *
     * @author Michael Edgar {@literal <michael@xlate.io>}
     */
    protected static class ParameterContext {
        protected String name;
        protected In location;
        protected Style style;
        protected Parameter oaiParam;
        protected FrameworkParameter frameworkParam;
        protected Object defaultValue;
        protected AnnotationTarget target;
        protected Type targetType;

        ParameterContext() {
        }

        public ParameterContext(String name, FrameworkParameter frameworkParam, AnnotationTarget target, Type targetType) {
            this.name = name;
            this.location = frameworkParam.location;
            this.style = frameworkParam.style;
            this.frameworkParam = frameworkParam;
            this.target = target;
            this.targetType = targetType;
        }

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
    protected static class ParameterContextKey {
        final String name;
        final In location;
        final Style style;
        final String ref;

        public ParameterContextKey(String name, In location, Style style) {
            this.name = name;
            this.location = location;
            this.style = style;
            this.ref = null;
        }

        public ParameterContextKey(Parameter oaiParam) {
            this.name = oaiParam.getName();
            this.location = oaiParam.getIn();
            this.style = styleOf(oaiParam);
            this.ref = oaiParam.getRef();
        }

        public ParameterContextKey(ParameterContext context) {
            this.name = context.name;
            this.location = context.location;
            this.style = context.style;
            this.ref = context.oaiParam != null ? context.oaiParam.getRef() : null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ParameterContextKey) {
                ParameterContextKey other = (ParameterContextKey) obj;

                if (isNull() && other.isNull()) {
                    return this == other;
                }

                if (ref != null) {
                    return ref.equals(other.ref);
                }

                return Objects.equals(this.name, other.name) &&
                        Objects.equals(this.location, other.location) &&
                        Objects.equals(this.style, other.style);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return isNull() ? super.hashCode() : Objects.hash(name, location, style, ref);
        }

        @Override
        public String toString() {
            return "name: " + name + "; in: " + location + "; style: " + style + "; ref: " + ref;
        }

        public boolean isNull() {
            return name == null && location == null && style == null && ref == null;
        }
    }

    protected AbstractParameterProcessor(AnnotationScannerContext scannerContext,
            String contextPath,
            Function<AnnotationInstance, Parameter> reader,
            List<AnnotationScannerExtension> extensions) {
        this.scannerContext = scannerContext;
        this.contextPath = contextPath;
        this.index = scannerContext.getIndex();
        this.readerFunction = reader;
        this.extensions = extensions;
        this.beanValidationScanner = scannerContext.getBeanValidationScanner();
    }

    protected void reset() {
        params.clear();
        formParams.clear();
        matrixParams.clear();
    }

    protected ResourceParameters process(ClassInfo resourceClass, MethodInfo resourceMethod) {

        ResourceParameters parameters = new ResourceParameters();

        processPathParameters(resourceClass, resourceMethod, parameters);

        // Clear Path-level parameters discovered and allows for processing operation-level parameters
        reset();

        processOperationParameters(resourceMethod, parameters);

        processFinalize(resourceClass, resourceMethod, parameters);

        return parameters;
    }

    protected void processPathParameters(ClassInfo resourceClass, MethodInfo resourceMethod, ResourceParameters parameters) {
        // Default no-op
    }

    protected void processOperationParameters(MethodInfo resourceMethod, ResourceParameters parameters) {
        List<MethodInfo> candidateMethods = scannerContext.getAugmentedIndex().ancestry(resourceMethod)
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        /*
         * Phase II - Read method arguments: @Parameter and framework's annotations
         *
         * Read the resource method and any method super classes/interfaces that it may override
         */
        candidateMethods.stream()
                .flatMap(m -> m.annotations().stream().filter(a -> !JandexUtil.equals(a.target(), m)))
                .forEach(this::readAnnotatedType);

        /*
         * Phase III - Read @Parameter(s) annotations directly on the record method
         *
         * Read the resource method and any method super classes/interfaces that it may override
         */
        candidateMethods.stream()
                .flatMap(m -> m.annotations().stream().filter(a -> JandexUtil.equals(a.target(), m)))
                .filter(a -> openApiParameterAnnotations.contains(a.name()))
                .forEach(this::readParameterAnnotation);

        parameters.setOperationParameters(getParameters(resourceMethod));
    }

    protected void processFinalize(ClassInfo resourceClass, MethodInfo resourceMethod, ResourceParameters parameters) {
        /*
         * Working list of all parameters.
         */
        List<Parameter> allParameters = parameters.getAllParameters();
        /*
         * Generate the path using the provided resource class, which may differ from the method's declaring
         * class - e.g. for inheritance.
         */
        parameters.setPathItemPath(generatePath(resourceClass, allParameters));
        parameters.setOperationPath(generatePath(resourceMethod, allParameters));

        parameters.getPathParameterTemplateNames()
                .stream()
                .filter(pip -> allParameters.stream().noneMatch(ap -> this.samePathParameter(ap, pip)))
                .map(pip -> getUnannotatedPathParameter(resourceMethod, pip))
                .filter(Objects::nonNull)
                .map(pip -> this.mapParameter(resourceMethod, pip))
                .forEach(parameters::addOperationParameter);

        // Re-sort (names of matrix parameters may have changed)
        parameters.sort(preferredOrder);

        parameters.setFormBodyContent(getFormBodyContent());
    }

    /**
     * Generate the path for the provided annotation target, either a class or a method.
     * Add the name of any discovered matrix parameters.
     *
     * @param target the target (either class or method)
     * @param parameters list of all parameters processed
     * @return the path for the target
     */
    protected String generatePath(AnnotationTarget target, List<Parameter> parameters) {
        final StringBuilder path = new StringBuilder(pathOf(target));

        if (path.length() > 0) {
            path.insert(0, '/');
        }

        /*
         * Search for path template variables where a regular expression
         * is specified, extract the pattern and apply to the parameter's schema
         * if no pattern is otherwise specified and the parameter is a string.
         */
        Matcher templateMatcher = getTemplateParameterPattern().matcher(path);

        while (templateMatcher.find()) {
            String variableName = templateMatcher.group(1).trim();
            String variablePattern = templateMatcher.group(2).trim();

            parameters.stream()
                    .filter(p -> samePathParameter(p, variableName))
                    .filter(this::templateParameterPatternEligible)
                    .forEach(p -> p.getSchema().setPattern(variablePattern));

            String replacement = templateMatcher.replaceFirst('{' + variableName + '}');
            path.setLength(0);
            path.append(replacement);

            templateMatcher = getTemplateParameterPattern().matcher(path);
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
                        ScannerSPILogging.log.missingPathSegment(segmentName);
                    }
                });

        return path.toString();
    }

    protected abstract Pattern getTemplateParameterPattern();

    boolean samePathParameter(Parameter param, String name) {
        return name.equals(param.getName())
                && Parameter.In.PATH.equals(param.getIn())
                && !Style.MATRIX.equals(param.getStyle());
    }

    /**
     * Determines if the parameter is eligible to have a pattern constraint
     * applied to its schema.
     *
     * @param param the parameter
     * @return true if the parameter may have the patter applied, otherwise false
     */
    boolean templateParameterPatternEligible(Parameter param) {
        Schema paramSchema = param.getSchema();
        List<SchemaType> schemaTypes = paramSchema != null ? paramSchema.getType() : Collections.emptyList();

        return Parameter.In.PATH.equals(param.getIn())
                && !Style.MATRIX.equals(param.getStyle())
                && paramSchema != null
                && schemaTypes.contains(SchemaType.STRING)
                && paramSchema.getPattern() == null;
    }

    /**
     * Performs the final merging of framework parameters with MP-OAI parameters to produce the list
     * of {@link Parameter}s found while scanning the current level (class or method).
     *
     * @param resourceMethod the method to which the returned parameters are applicable
     * @return list of {@link Parameter}s
     */
    protected List<Parameter> getParameters(MethodInfo resourceMethod) {
        List<Parameter> parameters;

        // Process any Matrix Parameters found
        mapMatrixParameters();

        // Convert ParameterContext entries to MP-OAI Parameters
        parameters = this.params.values()
                .stream()
                .map(context -> this.mapParameter(resourceMethod, context))
                .filter(Objects::nonNull)
                .sorted(ResourceParameters.parameterComparator(preferredOrder))
                .collect(Collectors.toList());

        return parameters.isEmpty() ? null : parameters;
    }

    private void mapMatrixParameters() {
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
                        generated.frameworkParam = getMatrixParameter();
                        generated.target = null;
                        generated.targetType = null;
                        generated.oaiParam = OASFactory.createParameter();
                        generated.oaiParam.setStyle(Style.MATRIX);
                        generated.oaiParam.setExplode(Boolean.TRUE);
                        params.put(new ParameterContextKey(generated), generated);
                        return generated;
                    });

            List<Schema> schemas = ModelUtil.getParameterSchemas(context.oaiParam);

            if (schemas.isEmpty()) {
                // ParameterContext was generated above or no @Schema was provided on the @Parameter style=matrix
                Schema schema = OASFactory.createSchema();
                schema.addType(SchemaType.OBJECT);
                ModelUtil.setParameterSchema(context.oaiParam, schema);
                schemas = Arrays.asList(schema);
            }

            for (Schema schema : schemas) {
                setSchemaProperties(schema, Collections.emptyMap(), matrixPath.getValue(), false);
            }
        }
    }

    protected abstract FrameworkParameter getMatrixParameter();

    /**
     *
     * @param resourceMethod method potentially containing an un-annotated path parameter argument
     * @param name name of the path parameter without any associated annotations
     * @return a new ParameterContext if the parameter is found, otherwise null
     */
    protected ParameterContext getUnannotatedPathParameter(MethodInfo resourceMethod, String name) {
        return null;
    }

    private Parameter mapParameter(MethodInfo resourceMethod, ParameterContext context) {
        Parameter param = context.oaiParam != null ? context.oaiParam : OASFactory.createParameter();

        if (context.frameworkParam == null) {
            scannerContext.getCurrentScanner().ifPresent(scanner -> {
                String paramIn = String.valueOf(context.oaiParam.getIn());
                ScannerSPILogging.log.missingFrameworkParam(scanner.getName(),
                        resourceMethod.declaringClass().toString(), resourceMethod.toString(), context.oaiParam.getName(),
                        paramIn);
            });
        }

        param.setName(context.name);

        mapParameterIn(param, context);

        if (isIgnoredParameter(param, resourceMethod)) {
            return null;
        }

        mapParameterStyle(param, context);
        mapParameterSchema(param, context);
        TypeUtil.mapDeprecated(scannerContext, context.target, param::getDeprecated, param::setDeprecated);
        mapParameterExtensions(param, context);

        if (param.getSchema() != null) {
            augmentParamSchema(param, context);
        }

        if (param.getRequired() == null && TypeUtil.isOptional(context.targetType)) {
            param.setRequired(Boolean.FALSE);
        }

        if (context.target != null && Extensions.getParamRef(param) == null) {
            Extensions.setParamRef(param, context.target);
        }

        return param;
    }

    void mapParameterIn(Parameter param, ParameterContext context) {
        if (param.getIn() == null && context.location != null) {
            param.setIn(context.location);
        }

        if (param.getIn() == In.PATH) {
            param.setRequired(true);
        }

    }

    void mapParameterStyle(Parameter param, ParameterContext context) {
        if (param.getStyle() == null && context.frameworkParam != null) {
            param.setStyle(context.frameworkParam.style);
        }
    }

    void mapParameterSchema(Parameter param, ParameterContext context) {
        if (ModelUtil.parameterHasSchema(param) || context.targetType == null) {
            return;
        }

        AnnotationInstance schemaAnnotation = scannerContext.annotations().getAnnotation(context.target,
                SchemaConstant.DOTNAME_SCHEMA);
        Schema schema;

        if (schemaAnnotation != null
                && Boolean.TRUE.equals(scannerContext.annotations().value(schemaAnnotation, SchemaConstant.PROP_HIDDEN))) {
            schema = null;
        } else if (schemaAnnotation != null) {
            Type paramType = scannerContext.annotations().value(schemaAnnotation, SchemaConstant.PROP_IMPLEMENTATION,
                    context.targetType);
            Map<String, Object> defaults;

            if (JandexUtil.isArraySchema(scannerContext, schemaAnnotation)
                    || TypeUtil.isTypeOverridden(scannerContext, context.targetType, schemaAnnotation)) {
                defaults = Collections.emptyMap();
            } else {
                defaults = TypeUtil.getTypeAttributes(paramType);
            }

            // readSchema *may* replace the existing schema, so we must assign.
            schema = SchemaFactory.readSchema(scannerContext, OASFactory.createSchema(), schemaAnnotation, defaults);
        } else {
            schema = SchemaFactory.typeToSchema(scannerContext, context.targetType, schemaAnnotation, extensions);
        }

        ModelUtil.setParameterSchema(param, schema);
    }

    void mapParameterExtensions(Parameter param, ParameterContext context) {
        if (param.getExtensions() == null) {
            param.setExtensions(scannerContext.io().extensionIO().readMap(context.target));
        }
    }

    /**
     * Attempt to resolve the type of targetType, using the full stack of resolvers
     * from the scanning context.
     *
     * @param targetType the type to resolve
     * @return the resolved type or targetType unchanged if resolution is not possible
     */
    protected Type resolveType(Type targetType) {
        if (targetType != null) {
            for (TypeResolver resolver : this.scannerContext.getResolverStack()) {
                targetType = resolver.resolve(targetType);
            }
        }

        return targetType;
    }

    /**
     * Scan the AnnotationTarget associated with Parameter <code>param</code> for supported
     * default value or Bean Validation annotations that may alter the schema. When
     * found, update param's schema either directly or, when a <code>ref</code> is present, by
     * creating a union of the local and reference schemas using <code>allOf</code>.
     *
     * @param param the Parameter containing a schema for update
     * @param context ParameterContext associated with param
     */
    void augmentParamSchema(Parameter param, ParameterContext context) {
        Schema paramSchema = param.getSchema();
        String ref = paramSchema.getRef();
        Schema localSchema;

        if (ref != null) {
            Type paramType = TypeUtil.unwrapType(context.targetType);

            /*
             * Lookup the schema `type` from components (if available) or guess the type if
             * the `ref` is not available.
             */
            Schema refSchema = ModelUtil.getComponent(scannerContext.getOpenApi(), ref);

            if (refSchema != null) {
                localSchema = OASFactory.createSchema().type(refSchema.getType());
            } else {
                localSchema = OASFactory.createSchema().addType(SchemaType
                        .valueOf(TypeUtil.getTypeAttributes(paramType).get(SchemaConstant.PROP_TYPE).toString().toUpperCase()));
            }
        } else {
            localSchema = paramSchema;
        }

        int modCount = SchemaSupport.getModCount(localSchema);

        if (beanValidationScanner.isPresent()) {
            beanValidationScanner.get().applyConstraints(context.target, localSchema, param.getName(),
                    (target, name) -> {
                        if (param.getRequired() == null) {
                            param.setRequired(Boolean.TRUE);
                        }
                    });
        }

        setDefaultValue(localSchema, context.defaultValue);

        if (localOnlySchemaModified(paramSchema, localSchema, modCount)) {
            // Add new `allOf` schema, erasing `type` derived above from the local schema
            Schema allOf = OASFactory.createSchema().addAllOf(paramSchema).addAllOf(localSchema.type((List<SchemaType>) null));
            param.setSchema(allOf);
        }
    }

    boolean localOnlySchemaModified(Schema paramSchema, Schema localSchema, int originalModCount) {
        int currentModCount = SchemaSupport.getModCount(localSchema);
        return localSchema != paramSchema && originalModCount != -1 && currentModCount > originalModCount;
    }

    /**
     * Converts the collection of parameter annotations to properties set on the
     * given schema.
     *
     * @param schema the {@link Schema} on which the properties will be set
     * @param encodings map of encodings applicable to the current {@link MediaType} being processed
     * @param params the name/value pairs of annotations for conversion to schema properties
     * @param schemaAnnotationSupported true if the parameter supports a co-located {@code @Schema} annotation
     */
    protected void setSchemaProperties(Schema schema,
            Map<String, Encoding> encodings,
            Map<String, AnnotationInstance> params,
            boolean schemaAnnotationSupported) {

        for (Entry<String, AnnotationInstance> param : params.entrySet()) {
            String paramName = param.getKey();
            AnnotationTarget paramTarget = param.getValue().target();
            Type paramType = getType(paramTarget);
            AnnotationInstance schemaAnnotation = null;
            if (schemaAnnotationSupported) {
                schemaAnnotation = scannerContext.annotations().getAnnotation(paramTarget, SchemaConstant.DOTNAME_SCHEMA);
            }
            Schema paramSchema = SchemaFactory.typeToSchema(scannerContext, paramType,
                    schemaAnnotation, extensions);

            if (paramSchema == null) {
                // hidden
                continue;
            }

            addEncoding(encodings, paramName, paramTarget);
            setDefaultValue(paramSchema, getDefaultValue(paramTarget));
            TypeUtil.mapDeprecated(scannerContext, paramTarget, paramSchema::getDeprecated, paramSchema::setDeprecated);

            if (beanValidationScanner.isPresent()) {
                beanValidationScanner.get().applyConstraints(paramTarget,
                        paramSchema,
                        paramName,
                        (target, name) -> setRequired(name, schema));
            }

            if (SchemaSupport.getNullable(paramSchema) == null && TypeUtil.isOptional(paramType)) {
                SchemaSupport.setNullable(paramSchema, Boolean.TRUE);
            }

            if (schema.getProperties() != null) {
                paramSchema = mergeObjects(schema.getProperties().get(paramName), paramSchema);
            }

            schema.addProperty(paramName, paramSchema);
        }
    }

    /**
     * Called by the BeanValidationScanner when a member is found to have a BV annotation
     * indicating the parameter is required.
     *
     * @param name name of the schema property
     * @param schema schema holding the property
     */
    private void setRequired(String name, Schema schema) {
        List<String> requiredProperties = schema.getRequired();

        if (requiredProperties == null || !requiredProperties.contains(name)) {
            schema.addRequired(name);
        }
    }

    private void setDefaultValue(Schema schema, Object defaultValue) {
        if (schema.getDefaultValue() == null && defaultValue != null) {
            schema.setDefaultValue(defaultValue);
        }
    }

    /**
     * Create a {@link Content} and use the scanned form parameters
     * as the properties. The media type will be determined by the framework's
     * subclass - e.g. for JAX-RS it may be defaulted to 'application/x-www-form-urlencoded' or set
     * to 'multipart/form-data' if a
     * RESTEasy <code>MultipartForm</code>
     * annotation was used to wrap the <code>javax.ws.rs.FormParam</code>s. The encoding values
     * for the {@link Content} will be set to the value of any RESTEasy <code>PartType</code>
     * annotations found for each parameter.
     *
     * @return generated form content
     */
    protected Content getFormBodyContent() {
        if (formParams.isEmpty()) {
            return null;
        }

        Content content = OASFactory.createContent();

        Arrays.stream(getFormMediaTypes(scannerContext::getCurrentConsumes, this::getFormMediaType))
                .forEach(mediaTypeName -> {
                    MediaType mediaType = OASFactory.createMediaType();
                    Schema schema = OASFactory.createSchema();
                    Map<String, Encoding> encodings = new HashMap<>();
                    schema.addType(SchemaType.OBJECT);
                    mediaType.setSchema(schema);

                    if (APPLICATION_FORM_URLENCODED.equals(mediaTypeName)) {
                        addFormParamEncodings(encodings);
                    }

                    setSchemaProperties(schema, encodings, formParams, true);

                    if (encodings.size() > 0) {
                        mediaType.setEncoding(encodings);
                    }

                    content.addMediaType(mediaTypeName, mediaType);
                });

        return content;
    }

    @SafeVarargs
    final String[] getFormMediaTypes(Supplier<String[]>... mediaTypeSources) {
        for (Supplier<String[]> souce : mediaTypeSources) {
            String[] mediaTypes = souce.get();
            if (mediaTypes != null) {
                return mediaTypes;
            }
        }
        return new String[] { getDefaultFormMediaType() };
    }

    String[] getFormMediaType() {
        return formMediaType != null ? new String[] { formMediaType } : null;
    }

    void addFormParamEncodings(Map<String, Encoding> encodings) {
        formParams.entrySet().stream()
                .map(param -> {
                    ParameterContextKey key = new ParameterContextKey(param.getKey(), null, Style.FORM);
                    return getParameterContext(key, param.getValue().target());
                })
                .filter(context -> context != null && context.oaiParam != null)
                .forEach(context -> {
                    Encoding encoding = null;

                    if (context.oaiParam.getStyle() != null) {
                        encoding = OASFactory.createEncoding();

                        String styleString = context.oaiParam.getStyle().toString();
                        Arrays.stream(Encoding.Style.values())
                                .filter(encodingStyle -> encodingStyle.toString().equals(styleString))
                                .findFirst()
                                .ifPresent(encoding::setStyle);
                    }

                    if (context.oaiParam.getExplode() != null) {
                        encoding = encoding != null ? encoding : OASFactory.createEncoding();
                        encoding.setExplode(context.oaiParam.getExplode());
                    }

                    if (encoding != null) {
                        encodings.put(context.name, encoding);
                    }
                });
    }

    protected String getDefaultFormMediaType() {
        return null;
    }

    /**
     * Determine if the paramTarget is annotated with the RestEasy
     * <code>PartType</code>
     * annotation and add the value to the encodings map.
     *
     * @param encodings map of encodings applicable to the current {@link MediaType} being processed
     * @param paramName name of the current form parameter being mapped to a schema property
     * @param paramTarget the target annotated with the framework's form annotation
     *
     */
    protected void addEncoding(Map<String, Encoding> encodings, String paramName, AnnotationTarget paramTarget) {
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
    protected boolean isIgnoredParameter(Parameter parameter, AnnotationTarget resourceMethod) {
        final String ref = parameter.getRef();
        final String paramName = parameter.getName();
        final In paramIn = parameter.getIn();

        if (paramIn == null && ref == null) {
            /*
             * Per @Parameter JavaDoc, ignored when empty string (i.e., unspecified).
             * This may occur when @Parameter is specified without a matching framework
             * parameter annotation.
             */
            return true;
        }

        if (Extensions.isHidden(parameter)) {
            return true;
        }

        /*
         * Name is REQUIRED unless it is a reference.
         */
        if ((paramName == null || paramName.trim().isEmpty()) && ref == null) {
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
            regex = String.format("(?:\\{[ \\t]*|^|/?)\\Q%s\\E(?:[ \\t]*(?:}|:)|/?|$)", paramName);
        } else {
            regex = String.format("\\{[ \\t]*\\Q%s\\E[ \\t]*(?:}|:)", paramName);
        }

        return Pattern.compile(regex).matcher(resourcePath).find();
    }

    /**
     * Read a single annotation that is either {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter
     * {@literal @}Parameter} or
     * {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameters {@literal @}Parameters}. The results are stored
     * in the private {@link #params}
     * collection.
     *
     * @param annotation a parameter annotation to be read and processed
     */
    protected void readParameterAnnotation(AnnotationInstance annotation) {
        DotName name = annotation.name();

        if (Names.PARAMETER.equals(name)) {
            readAnnotatedType(annotation, null, false);
        } else if (Names.PARAMETERS.equals(name)) {
            AnnotationValue annotationValue = annotation.value();

            if (annotationValue != null) {
                AnnotationInstance[] parameters = annotationValue.asNestedArray();
                preferredOrder = new ArrayList<>(parameters.length);

                /*
                 * Unwrap annotations wrapped by @Parameters and
                 * identify the target as the target of the @Parameters annotation
                 */
                for (AnnotationInstance nested : parameters) {
                    readAnnotatedType(AnnotationInstance.create(nested.name(),
                            annotation.target(),
                            nested.values()),
                            null,
                            false);
                }
            }
        }
    }

    /**
     * Read a single annotation that is either {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter
     * {@literal @}Parameter} or
     * one of the framework parameter annotations. The results are stored in the
     * private {@link #params} collection, depending on the type of parameter.
     *
     * @param annotation a parameter annotation to be read and processed
     */
    protected void readAnnotatedType(AnnotationInstance annotation) {
        readAnnotatedType(annotation, null, false);
    }

    /**
     * Read a single annotation that is either {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter
     * {@literal @}Parameter} or
     * one of the framework parameter annotations. The results are stored in the
     * private {@link #params} collection. When overriddenParametersOnly is true,
     * new parameters not already known in {@link #params} will be ignored.
     *
     * @param annotation a parameter annotation to be read and processed
     * @param beanParamAnnotation a framework's bean-type (POJO) parameter annotation
     * @param overriddenParametersOnly true if only parameters already known to the scanner are considered, false otherwise
     */
    protected abstract void readAnnotatedType(AnnotationInstance annotation, AnnotationInstance beanParamAnnotation,
            boolean overriddenParametersOnly);

    /**
     * Retrieves either the provided parameter {@link Parameter.Style}, the default
     * style of the parameter based on the <code>in</code> attribute, or null if <code>in</code> is not defined.
     *
     * @param param the {@link Parameter}
     * @return the param's style, the default style defined based on <code>in</code>, or null if <code>in</code> is not defined.
     */
    protected static Style styleOf(Parameter param) {
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
     * Set this {@link AbstractParameterProcessor}'s formMediaType if it has not already
     * been set and the value is explicitly known for the parameter type.
     *
     * @param frameworkParam parameter to check for a form media type
     *
     */
    protected void setMediaType(FrameworkParameter frameworkParam) {
        if (frameworkParam.mediaType != null && this.formMediaType == null) {
            formMediaType = frameworkParam.mediaType;
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
    protected static String paramName(AnnotationInstance annotation) {
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
                if (method.parametersCount() == 1) {
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

    protected Set<DotName> getDefaultAnnotationNames() {
        return Collections.emptySet();
    }

    protected String getDefaultAnnotationProperty() {
        return null;
    }

    /**
     * Scan and parse a default value annotation.
     * If the target is a Java primitive, the value will be parsed into an equivalent
     * wrapper object.
     *
     * @param target target annotated with a parameter annotation
     * @return the default value
     */
    protected Object getDefaultValue(AnnotationTarget target) {
        Set<DotName> defaultAnnotationNames = getDefaultAnnotationNames();
        String annotationProperty = getDefaultAnnotationProperty();

        if (defaultAnnotationNames == null || defaultAnnotationNames.isEmpty() || annotationProperty == null) {
            return null;
        }

        AnnotationInstance defaultValueAnno = scannerContext.annotations().getAnnotation(target, defaultAnnotationNames);
        Object defaultValue = null;

        if (defaultValueAnno != null) {
            String defaultValueString = scannerContext.annotations().value(defaultValueAnno, annotationProperty);
            defaultValue = defaultValueString;
            Type targetType = getType(target);

            if (targetType != null && targetType.kind() == Type.Kind.PRIMITIVE) {
                Primitive primitive = targetType.asPrimitiveType().primitive();
                Object primitiveValue = primitiveToObject(primitive, defaultValueString);

                if (primitiveValue != null) {
                    defaultValue = primitiveValue;
                }
            }
        }
        return defaultValue;
    }

    protected Object primitiveToObject(Primitive primitive, String stringValue) {
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
            ScannerSPILogging.log.invalidDefault(stringValue, primitive.name().toLowerCase());
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
    protected String lastPathSegmentOf(AnnotationTarget target) {
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
     * Find the full path of the target, including parent resources if the annotation target is a member of a sub-resource
     * class. Method-level targets will include both the path to the resource and the path to the method joined with a '/'.
     *
     * @param target target item for which the path is being generated
     * @return full path (excluding application path) of the target
     */
    protected String fullPathOf(AnnotationTarget target) {
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

        return contextPath + '/' + pathSegment;
    }

    /**
     * Concatenate the method's path with the path of its declaring
     * class.
     *
     * @param method the method annotated with the framework's path annotation
     */
    String methodPath(MethodInfo method) {
        String methodPath = pathOf(method);
        String classPath = pathOf(method.declaringClass());

        if (methodPath.isEmpty()) {
            return classPath;
        }

        return classPath + '/' + methodPath;
    }

    /**
     * Reads the framework's path annotation present on the
     * target and strips leading and trailing slashes.
     *
     * @param target target object
     * @return value of the framework's path annotation without
     *         leading/trailing slashes.
     */
    protected abstract String pathOf(AnnotationTarget target);

    /**
     * Determines the type of the target. Method annotations will give
     * the name of a single argument, assumed to be a "setter" method.
     *
     * @param target target object
     * @return object type
     */
    protected static Type getType(AnnotationTarget target) {
        if (target == null) {
            return null;
        }

        Type type = null;

        switch (target.kind()) {
            case FIELD:
                type = target.asField().type();
                break;
            case METHOD:
                List<Type> methodParams = target.asMethod().parameterTypes();
                if (methodParams.size() == 1) {
                    // This is a bean property setter
                    type = methodParams.get(0);
                }
                break;
            case METHOD_PARAMETER:
                type = target.asMethodParameter().method().parameterType(target.asMethodParameter().position());
                break;
            default:
                break;
        }

        return type;
    }

    protected boolean isReadableParameterAnnotation(DotName name) {
        return Names.PARAMETER.equals(name) && readerFunction != null;
    }

    protected void readParameterAnnotation(AnnotationInstance annotation, boolean overriddenParametersOnly) {
        Parameter oaiParam = readerFunction.apply(annotation);

        if (oaiParam.getRef() != null) {
            Parameter commonParam = ModelUtil.dereference(scannerContext.getOpenApi(), oaiParam);
            oaiParam.setName(commonParam.getName());
            oaiParam.setIn(commonParam.getIn());
            if (commonParam.getIn() == In.PATH) {
                oaiParam.setRequired(true);
            }
            oaiParam.setStyle(commonParam.getStyle());
        }

        readParameter(new ParameterContextKey(oaiParam),
                oaiParam,
                null,
                null,
                annotation.target(),
                overriddenParametersOnly);
    }

    protected void readFrameworkParameter(AnnotationInstance annotation, FrameworkParameter frameworkParam,
            boolean overriddenParametersOnly) {
        AnnotationTarget target = annotation.target();

        readParameter(
                new ParameterContextKey(paramName(annotation), frameworkParam.location, frameworkParam.defaultStyle),
                null,
                frameworkParam,
                getDefaultValue(target),
                target,
                overriddenParametersOnly);
    }

    /**
     * Merges MP-OAI {@link Parameter}s and framework-specific parameters for the same {@link In} and name,
     * and {@link Style}. When overriddenParametersOnly is true, new parameters not already known
     * in {@link #params} will be ignored.
     *
     * The given {@link ParameterContextKey key} contains:
     *
     * <ul>
     * <li>the name of the parameter specified by application
     * <li>location, given by {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter#in @Parameter.in}
     * or implied by the type of framework annotation used on the target
     * <li>style, the parameter's style, either specified by the application or implied by the parameter's location
     * </ul>
     *
     * @param key the key for the parameter being processed
     * @param oaiParam scanned {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter @Parameter}
     * @param frameworkParam Meta detail about the framework parameter being processed, if found.
     * @param defaultValue value read from the framework's default-value annotation.
     * @param target target of the annotation
     * @param overriddenParametersOnly true if only parameters already known to the scanner are considered, false otherwise
     */
    protected void readParameter(ParameterContextKey key,
            Parameter oaiParam,
            FrameworkParameter frameworkParam,
            Object defaultValue,
            AnnotationTarget target,
            boolean overriddenParametersOnly) {

        //TODO: Test to ensure @Parameter attributes override framework for the same parameter
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

        if (context.frameworkParam == null) {
            context.frameworkParam = frameworkParam;
            context.defaultValue = defaultValue;
        }

        if (context.target == null || context.target.kind() == Kind.METHOD) {
            context.target = target;
            context.targetType = this.resolveType(getType(target));
        }

        if (addParam) {
            params.put(new ParameterContextKey(context), context);
        }

        if (preferredOrder != null) {
            preferredOrder.add(context.oaiParam);
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
                    // Only attempt to match parameters without `ref` specified
                    .filter(c -> c.oaiParam == null || c.oaiParam.getRef() == null)
                    .filter(c -> haveSameAnnotatedTarget(c, target, key.name))
                    .filter(c -> attributesCompatible(c.location, key.location))
                    .filter(c -> attributesCompatible(c.style, key.style))
                    .findFirst()
                    .orElseGet(() -> params.values()
                            .stream()
                            .filter(c -> nameAndStyleMatch(c, key))
                            .findFirst()
                            .orElse(null));
        }

        return context;
    }

    boolean haveSameAnnotatedTarget(ParameterContext context, AnnotationTarget target, String name) {
        boolean sameTarget = false;

        if (JandexUtil.equals(target, context.target)) {
            if (target.kind() != Kind.METHOD) {
                // Both annotations are set on the same method argument or field
                sameTarget = true;
            } else {
                /*
                 * Method targets: consider names to match if *one* is unspecified or they are equal.
                 */
                if (name != null) {
                    sameTarget = context.name == null || Objects.equals(name, context.name);
                } else if (context.name != null) {
                    sameTarget = true;
                }
            }
        } else if (JandexUtil.equals(targetMethod(target), targetMethod(context.target))) {
            /*
             * One annotation is on the method and the other is on a parameter. Require that
             * the names are both given and match.
             */
            sameTarget = name != null && name.equals(context.name);
        }

        return sameTarget;
    }

    boolean attributesCompatible(Object a1, Object a2) {
        return a1 == null || a2 == null || a1.equals(a2);
    }

    boolean nameAndStyleMatch(ParameterContext context, ParameterContextKey key) {
        /*
         * Allow a match on just the name and style if one of the Parameter.In values
         * is not specified
         */
        if ((context.location == null || key.location == null) && key.name != null && key.style != null) {
            return key.name.equals(context.name) && key.style.equals(context.style);
        }

        return false;
    }

    /**
     * Obtain the MethodInfo associated with the annotation target.
     *
     * @param target annotated item. Only method and method parameter targets.
     * @return the MethodInfo associated with the target, or null if target is not a method or parameter.
     */
    static MethodInfo targetMethod(AnnotationTarget target) {
        if (target.kind() == Kind.METHOD) {
            return target.asMethod();
        }
        if (target.kind() == Kind.METHOD_PARAMETER) {
            return target.asMethodParameter().method();
        }
        return null;
    }

    /**
     * Scans for class level parameters on the given class argument and its ancestors.
     *
     * @param clazz the class to be scanned for parameters.
     * @param beanParamAnnotation the bean parameter annotation to be used for path derivation
     * @param overriddenParametersOnly true if only parameters already known to the scanner are considered, false otherwise
     */
    protected void readParametersInherited(ClassInfo clazz, AnnotationInstance beanParamAnnotation,
            boolean overriddenParametersOnly) {
        AugmentedIndexView augmentedIndex = scannerContext.getAugmentedIndex();
        List<ClassInfo> ancestors = new ArrayList<>(augmentedIndex.inheritanceChain(clazz, null).keySet());
        /*
         * Process parent class(es) before the resource method class to allow for overridden parameter attributes.
         */
        Collections.reverse(ancestors);

        ancestors.forEach(c -> {
            c.interfaceTypes()
                    .stream()
                    .map(augmentedIndex::getClass)
                    .filter(Objects::nonNull)
                    .forEach(iface -> readParameters(iface, beanParamAnnotation, overriddenParametersOnly));

            readParameters(c, beanParamAnnotation, overriddenParametersOnly);
        });
    }

    /**
     * Scans for class level parameters. This method is used for both resource class
     * annotation scanning and framework-specific 'bean' parameter type target type scanning.
     *
     * @param clazz the class to be scanned for parameters.
     * @param beanParamAnnotation the bean parameter annotation to be used for path derivation
     * @param overriddenParametersOnly true if only parameters already known to the scanner are considered, false otherwise
     */
    protected void readParameters(ClassInfo clazz, AnnotationInstance beanParamAnnotation, boolean overriddenParametersOnly) {
        clazz.annotationsMap()
                .entrySet()
                .stream()
                .filter(e -> Names.PARAMETER.equals(e.getKey()) || isParameter(e.getKey()))
                .flatMap(a -> a.getValue().stream())
                .filter(this::isBeanPropertyParam)
                .forEach(annotation -> readAnnotatedType(annotation, beanParamAnnotation, overriddenParametersOnly));
    }

    /**
     * Determines if the annotation is a property parameter. Annotation targets
     * must be annotated with a framework-specific parameter annotation or
     * {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter @Parameter}.
     *
     * Method targets must not be annotated with one of the framework-specific HTTP method annotations and
     * the method must have a single argument.
     *
     * @param annotation
     * @return
     */
    boolean isBeanPropertyParam(AnnotationInstance annotation) {
        AnnotationTarget target = annotation.target();
        boolean relevant = false;

        switch (target.kind()) {
            case FIELD: {
                FieldInfo field = target.asField();
                relevant = hasParameters(field.annotations());
                break;
            }

            case METHOD_PARAMETER: {
                MethodParameterInfo param = target.asMethodParameter();
                MethodInfo method = param.method();
                relevant = nonSyntheticParameterMethod(method,
                        scannerContext.annotations().getMethodParameterAnnotations(method, param.position()));
                break;
            }

            case METHOD: {
                MethodInfo method = target.asMethod();
                relevant = nonSyntheticParameterMethod(method, method.annotations()) &&
                        getType(target) != null;
                break;
            }

            default:
                break;
        }

        return relevant;
    }

    boolean nonSyntheticParameterMethod(MethodInfo method, Collection<AnnotationInstance> annotations) {
        return !method.isSynthetic() &&
                !isResourceMethod(method) &&
                hasParameters(annotations) &&
                !isSubResourceLocator(method);
    }

    /**
     * Determines if the given method is a sub-resource locator method
     * annotated by {@code @Path} but NOT annotated with one of the HTTP method
     * annotations.
     *
     * @param method method to check
     * @return true if the method is sub-resource locator, false otherwise
     */
    protected abstract boolean isSubResourceLocator(MethodInfo method);

    /**
     * Determines if the given method is a framework resource method annotated by one
     * of the HTTP method annotations.
     *
     * @param method method to check
     * @return true if the method is annotated with a framework-specific HTTP method annotation, false otherwise
     */
    protected abstract boolean isResourceMethod(MethodInfo method);

    /**
     * Check for the existence relevant parameter annotations in the collection.
     *
     * @param annotations collection of annotations
     * @return true if any of the annotations is a relevant parameter annotation.
     */
    protected boolean hasParameters(Collection<AnnotationInstance> annotations) {
        return annotations.stream()
                .map(AnnotationInstance::name)
                .anyMatch(this::isParameter);
    }

    protected abstract boolean isParameter(DotName annotationName);
}
