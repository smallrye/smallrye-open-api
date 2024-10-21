package io.smallrye.openapi.runtime.scanner.spi;

import static org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

import io.smallrye.openapi.api.OpenApiConfig.DuplicateOperationIdBehavior;
import io.smallrye.openapi.api.OpenApiConfig.OperationIdStrategy;
import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.constants.JacksonConstants;
import io.smallrye.openapi.api.constants.KotlinConstants;
import io.smallrye.openapi.api.constants.SecurityConstants;
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
import io.smallrye.openapi.runtime.scanner.processor.JavaSecurityProcessor;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * This represent a scanner
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface AnnotationScanner {

    public String getName();

    // Scan using this scanner
    public OpenAPI scan(final AnnotationScannerContext annotationScannerContext, OpenAPI oai);

    // Create an APIResponse from the response, we need the following info
    public boolean isAsyncResponse(final MethodInfo method);

    public boolean isPostMethod(final MethodInfo method);

    public boolean isDeleteMethod(final MethodInfo method);

    public boolean containsScannerAnnotations(List<AnnotationInstance> instances,
            List<AnnotationScannerExtension> extensions);

    // Allow runtimes to set the context root path
    public void setContextRoot(String path);

    public String[] getDefaultConsumes(AnnotationScannerContext context, MethodInfo methodInfo, ResourceParameters params);

    public String[] getDefaultProduces(AnnotationScannerContext context, MethodInfo methodInfo);

    default boolean isMultipartOutput(Type returnType) {
        return false;
    }

    default boolean isMultipartInput(Type inputType) {
        return false;
    }

    default boolean isScannerInternalResponse(Type returnType) {
        return false;
    }

    default boolean isScannerInternalParameter(Type parameterType) {
        return false;
    }

    // For wrapped type (other than Optional) - default no others
    default boolean isWrapperType(Type type) {
        return false;
    }

    default Type unwrapType(Type type) {
        if (isWrapperType(type)) {
            return type.asParameterizedType().arguments().get(0);
        }
        return null;
    }

    /**
     * Process a certain class for OpenApiDefinition annotations.
     *
     * @param context the scanning context
     * @param targetClass the class that contain the server annotation
     */
    default OpenAPI processDefinitionAnnotation(final AnnotationScannerContext context, final ClassInfo targetClass) {
        return Optional.ofNullable(context.io().openApiDefinitionIO().read(targetClass))
                .orElseGet(() -> OASFactory.createOpenAPI().openapi(SmallRyeOASConfig.Defaults.VERSION));
    }

    /**
     * Process a certain class for security annotations.
     *
     * @param targetClass the class that contain the security annotation
     * @param openApi the current OpenApi model being created
     */
    default void processSecuritySchemeAnnotation(final AnnotationScannerContext context, final ClassInfo targetClass,
            OpenAPI openApi) {

        context.io().securityIO().readSchemes(targetClass)
                .forEach((name, scheme) -> ModelUtil.components(openApi).addSecurityScheme(name, scheme));
    }

    /**
     * Process a certain class for server annotations.
     *
     * @param targetClass the class that contain the server annotation
     * @param openApi the current OpenApi model being created
     */
    default void processServerAnnotation(final AnnotationScannerContext context, final ClassInfo targetClass, OpenAPI openApi) {
        context.io().serverIO().readList(targetClass).forEach(openApi::addServer);
    }

    /**
     * Process Java security (roles allowed and declared roles)
     *
     * @param openApi the OpenAPI Model
     * @param resourceClass the Class being scanned
     */
    default void processJavaSecurity(AnnotationScannerContext context, ClassInfo resourceClass, OpenAPI openApi) {
        JavaSecurityProcessor securityProcessor = context.getJavaSecurityProcessor();
        securityProcessor.initialize(openApi);
        securityProcessor
                .addDeclaredRolesToScopes(
                        context.annotations().getAnnotationValue(resourceClass, SecurityConstants.DECLARE_ROLES));
        securityProcessor
                .addRolesAllowedToScopes(
                        context.annotations().getAnnotationValue(resourceClass, SecurityConstants.ROLES_ALLOWED));
    }

    /**
     * Process tags.
     * Tag and Tags annotations combines with the resource tags we've already found (passed in)
     *
     * @param method the REST method
     * @param openApi the OpenApi model
     * @param resourceTags tags passed in
     * @param operation the current operation
     */
    default void processOperationTags(final AnnotationScannerContext context, final MethodInfo method, OpenAPI openApi,
            Set<String> resourceTags,
            final Operation operation) {
        //
        Set<String> tags = processTags(context, method, openApi, true);
        if (tags == null) {
            if (!resourceTags.isEmpty()) {
                operation.setTags(new ArrayList<>(resourceTags));
            }
        } else if (!tags.isEmpty()) {
            operation.setTags(new ArrayList<>(tags));
        }
    }

    /**
     * Processes any {@link org.eclipse.microprofile.openapi.annotations.tags.Tag} or
     * {@link org.eclipse.microprofile.openapi.annotations.tags.Tags} annotations present on
     * the annotation target and adds them to the OpenAPI model. The set of tag names found
     * (with iteration order preserved) is returned.
     *
     * @param openApi OpenAPI model
     * @param target a MethodInfo or ClassInfo to read for tag annotations
     * @param nullWhenMissing determines if an empty set or a null value is returned when no annotations are found.
     * @return the set of tag names found
     */
    default Set<String> processTags(final AnnotationScannerContext context, final AnnotationTarget target, OpenAPI openApi,
            final boolean nullWhenMissing) {

        if (!context.io().tagIO().hasRepeatableAnnotation(target)) {
            return nullWhenMissing ? null : Collections.emptySet();
        }

        Set<String> tags = new LinkedHashSet<>();

        context.io().tagIO().readList(target)
                .stream()
                .filter(tag -> Objects.nonNull(tag.getName()))
                .forEach(tag -> {
                    tags.add(tag.getName());
                    ModelUtil.addTag(openApi, tag);
                });

        context.io().tagIO().readReferences(target).forEach(tags::add);

        return tags;
    }

    /**
     * Extracts all methods from the provided class and its ancestors that are known to the instance's index
     *
     * @param context the scanning context
     * @param resource the resource class
     * @return all methods from the provided class and its ancestors
     */
    default List<MethodInfo> getResourceMethods(final AnnotationScannerContext context, ClassInfo resource) {
        Type resourceType = Type.create(resource.name(), Type.Kind.CLASS);
        AugmentedIndexView index = context.getAugmentedIndex();
        Map<ClassInfo, Type> chain = index.inheritanceChain(resource, resourceType);
        List<MethodInfo> methods = new ArrayList<>();

        for (ClassInfo classInfo : chain.keySet()) {
            classInfo.methods()
                    .stream()
                    .filter(method -> !method.isSynthetic())
                    .forEach(methods::add);

            index.interfaces(classInfo)
                    .stream()
                    .filter(type -> !TypeUtil.knownJavaType(type.name()))
                    .map(context.getAugmentedIndex()::getClass)
                    .filter(Objects::nonNull)
                    .flatMap(iface -> iface.methods().stream())
                    .forEach(methods::add);
        }

        return methods;
    }

    /**
     * While scanning JAX-RS/Spring method, find the operations
     *
     * @param context the scanning context
     * @param resourceClass the JAX-RS/Spring concrete resource class
     * @param method the JAX-RS/Spring method
     * @return Maybe an Operation model
     */
    default Optional<Operation> processOperation(final AnnotationScannerContext context,
            final ClassInfo resourceClass,
            final MethodInfo method) {
        if (context.io().operationIO().isHidden(method)) {
            return Optional.empty();
        }

        Operation operation = context.io().operationIO().read(method);

        if (operation == null) {
            operation = OASFactory.createOperation();
        }

        Extensions.setMethodRef(operation, resourceClass, method);

        // @Deprecrated may be on either the method or the class
        TypeUtil.mapDeprecated(context, method, operation::getDeprecated, operation::setDeprecated);
        TypeUtil.mapDeprecated(context, resourceClass, operation::getDeprecated, operation::setDeprecated);

        OperationIdStrategy operationIdStrategy = context.getConfig().getOperationIdStrategy();

        if (operationIdStrategy != null && operation.getOperationId() == null) {
            String operationId = null;

            switch (operationIdStrategy) {
                case METHOD:
                    operationId = method.name();
                    break;
                case CLASS_METHOD:
                    operationId = resourceClass.name().withoutPackagePrefix() + "_" + method.name();
                    break;
                case PACKAGE_CLASS_METHOD:
                    operationId = resourceClass.name() + "_" + method.name();
                    break;
                default:
                    break;
            }

            operation.setOperationId(operationId);
        }

        context.getOperationHandler().handleOperation(operation, resourceClass, method);

        // validate operationId
        String operationId = operation.getOperationId();
        if (operationId != null) {
            final MethodInfo conflictingMethod = context.getOperationIdMap().putIfAbsent(operationId, method);
            if (conflictingMethod != null) {
                final ClassInfo conflictingClass = conflictingMethod.declaringClass();
                final String className = resourceClass.name().toString();
                final String methodName = method.toString();
                final String conflictingClassName = conflictingClass.name().toString();
                final String conflictingMethodName = conflictingMethod.toString();
                if (context.getConfig().getDuplicateOperationIdBehavior() == DuplicateOperationIdBehavior.WARN) {
                    ScannerSPILogging.log.duplicateOperationId(operationId, className, methodName,
                            conflictingClassName, conflictingMethodName);
                } else {
                    throw ScannerSPIMessages.msg.duplicateOperationId(operationId, className, methodName,
                            conflictingClassName, conflictingMethodName);
                }
            }
        }

        return Optional.of(operation);
    }

    default void setJsonViewContext(AnnotationScannerContext context, Type[] views) {
        clearJsonViewContext(context);

        if (views != null && views.length > 0) {
            AugmentedIndexView index = context.getAugmentedIndex();

            Arrays.stream(views)
                    .map(viewType -> {
                        if (index.containsClass(viewType)) {
                            return index.inheritanceChain(index.getClass(viewType), viewType).values();
                        }
                        return Collections.singleton(viewType);
                    })
                    .flatMap(Collection::stream)
                    .forEach(context.getJsonViews()::add);
        }
    }

    default void clearJsonViewContext(AnnotationScannerContext context) {
        context.getJsonViews().clear();
    }

    default void processResponse(final AnnotationScannerContext context, final ClassInfo resourceClass, final MethodInfo method,
            Operation operation,
            Map<DotName, Map<String, APIResponse>> exceptionResponseMap) {

        setJsonViewContext(context, context.annotations().getAnnotationValue(method, JacksonConstants.JSON_VIEW));

        Optional<APIResponses> classResponses = Optional.ofNullable(context.io().apiResponsesIO().read(resourceClass));
        Map<String, APIResponse> classResponse = context.io().apiResponsesIO().readSingle(resourceClass);
        addResponses(operation, classResponses, classResponse, false);

        // Method annotations override class annotations
        Optional<APIResponses> methodResponses = Optional.ofNullable(context.io().apiResponsesIO().read(method));
        Map<String, APIResponse> methodResponse = context.io().apiResponsesIO().readSingle(method);
        addResponses(operation, methodResponses, methodResponse, true);

        context.io().apiResponsesIO().readResponseSchema(method)
                .ifPresent(responseSchema -> addApiReponseSchemaFromAnnotation(responseSchema, method, operation));

        /*
         * If there is no response from annotations, try to create one from the method return value.
         * Do not generate a response if the app has used an empty @ApiResponses annotation. This
         * provides a way for the application to indicate that responses will be supplied some other
         * way (i.e. static file).
         */
        if (methodResponses.isPresent() || context.io().apiResponsesIO().getAnnotation(method) == null) {
            createResponseFromRestMethod(context, method, operation);
        }

        //Add api response using list of exceptions in the methods and exception mappers
        List<Type> methodExceptions = method.exceptions();

        if (exceptionResponseMap != null) {
            methodExceptions.stream()
                    .map(Type::name)
                    .filter(exceptionResponseMap::containsKey)
                    .map(exceptionResponseMap::get)
                    .forEach(responseMap -> responseMap.forEach((code, response) -> {
                        APIResponses responses = ModelUtil.responses(operation);

                        if (!responses.hasAPIResponse(code)) {
                            responses.addAPIResponse(code, response);
                        }
                    }));
        }

        clearJsonViewContext(context);
    }

    default void addResponses(Operation operation, Optional<APIResponses> responses, Map<String, APIResponse> singleResponse,
            boolean includeExtensions) {
        responses.ifPresent(resp -> {
            resp.getAPIResponses().forEach(ModelUtil.responses(operation)::addAPIResponse);
            if (includeExtensions && resp.getExtensions() != null) {
                resp.getExtensions().forEach(ModelUtil.responses(operation)::addExtension);
            }
        });
        if (singleResponse != null) {
            singleResponse.forEach(ModelUtil.responses(operation)::addAPIResponse);
        }
    }

    /**
     * Called when a scanner (jax-rs, spring) method's APIResponse annotations have all been processed but
     * no response was actually created for the operation.This method will create a response
     * from the method information and add it to the given operation. It will try to do this
     * by examining the method's return value and the type of operation (GET, PUT, POST, DELETE).
     *
     * If there is a return value of some kind (a non-void return type) then the response code
     * is assumed to be 200.
     *
     * If there not a return value (void return type) then either a 201 or 204 is returned,
     * depending on the type of request.
     *
     * @param context the scanning context
     * @param method the current method
     * @param operation the current operation
     */
    default void createResponseFromRestMethod(final AnnotationScannerContext context,
            final MethodInfo method,
            Operation operation) {

        Type returnType = context.getResourceTypeResolver().resolve(method.returnType());
        APIResponse response = null;
        final int status = getDefaultStatus(method);
        final String code = String.valueOf(status);
        final String description = getReasonPhrase(status);

        if (isVoidResponse(method)) {
            if (generateResponse(code, operation)) {
                response = OASFactory.createAPIResponse().description(description);
            }
        } else if (generateResponse(code, operation)) {
            response = OASFactory.createAPIResponse().description(description);

            /*
             * Only generate content if not already supplied in annotations and the
             * method does not return an opaque Scanner Response
             */
            if (!isScannerInternalResponse(returnType, context, method) &&
                    (ModelUtil.responses(operation).getAPIResponse(code) == null ||
                            ModelUtil.responses(operation).getAPIResponse(code).getContent() == null)) {

                Schema schema;

                if (isMultipartOutput(returnType)) {
                    schema = OASFactory.createSchema().addType(Schema.SchemaType.OBJECT);
                } else if (hasKotlinContinuation(method)) {
                    schema = kotlinContinuationToSchema(context, method);
                } else {
                    schema = SchemaFactory.typeToSchema(context, returnType, null, context.getExtensions());
                }

                Content content = OASFactory.createContent();
                String[] produces = context.getCurrentProduces();

                if (produces == null || produces.length == 0) {
                    produces = getDefaultProduces(context, method);
                }

                if (schema != null && SchemaSupport.getNullable(schema) == null
                        && TypeUtil.isOptional(returnType)) {
                    if (schema.getType() != null) {
                        SchemaSupport.setNullable(schema, Boolean.TRUE);
                    }
                    if (schema.getRef() != null) {
                        // Move reference to type into its own subschema
                        Schema refSchema = OASFactory.createSchema().ref(schema.getRef());
                        schema.setRef(null);
                        if (schema.getAnyOf() == null) {
                            schema.addAnyOf(refSchema)
                                    .addAnyOf(SchemaSupport.nullSchema());
                        } else {
                            Schema anyOfSchema = OASFactory.createSchema()
                                    .addAnyOf(refSchema)
                                    .addAnyOf(SchemaSupport.nullSchema());
                            schema.addAllOf(anyOfSchema);
                        }
                    }
                }

                for (String producesType : produces) {
                    MediaType mt = OASFactory.createMediaType();
                    mt.setSchema(schema);
                    content.addMediaType(producesType, mt);
                }

                response.setContent(content);
            }
        }

        if (response != null) {
            APIResponses responses = ModelUtil.responses(operation);

            if (responses.hasAPIResponse(code)) {
                APIResponse responseFromAnnotations = responses.getAPIResponse(code);
                // Overlay the information from the annotations (2nd arg) onto the generated details (1st)
                response = MergeUtil.mergeObjects(response, responseFromAnnotations);
            }

            responses.addAPIResponse(code, response);
        }
    }

    /**
     * Derives a default HTTP status code for the provided REST endpoint implementation
     * method using the rules defined by {@literal @}APIResponseSchema#responseCode().
     *
     * @param method the endpoint method
     * @return the derived HTTP status
     */
    default int getDefaultStatus(final MethodInfo method) {
        final int status;

        if (isVoidResponse(method)) {
            if (isPostMethod(method)) {
                status = 201; // Created
            } else if (!isAsyncResponse(method)) {
                status = 204; // No Content
            } else {
                status = 200; // OK
            }
        } else {
            status = 200; // OK
        }

        return status;
    }

    default boolean isVoidResponse(final MethodInfo method) {
        return isVoidType(method.returnType());
    }

    default boolean isVoidType(Type type) {
        if (TypeUtil.isVoid(type)) {
            return true;
        } else if (isWrapperType(type)) {
            ParameterizedType parameterizedType = type.asParameterizedType();
            return parameterizedType.arguments().stream().anyMatch(this::isVoidType);
        } else if (TypeUtil.isWrappedType(type)) {
            return isVoidType(TypeUtil.unwrapType(type));
        }

        return false;
    }

    default boolean isScannerInternalResponse(Type returnType, AnnotationScannerContext context, MethodInfo method) {
        if (isScannerInternalResponse(returnType)) {
            return true;
        }

        return hasKotlinContinuation(method) &&
                isScannerInternalResponse(getKotlinContinuationArgument(context, method));
    }

    default boolean hasKotlinContinuation(MethodInfo method) {
        return method.parameterTypes().stream().anyMatch(this::isKotlinContinuation);
    }

    default boolean isKotlinContinuation(Type paramType) {
        return KotlinConstants.CONTINUATION.equals(paramType.name());
    }

    default Type getKotlinContinuationArgument(AnnotationScannerContext context, MethodInfo method) {
        Type type = method.parameterTypes()
                .stream()
                .filter(this::isKotlinContinuation)
                .findFirst()
                .map(context.getResourceTypeResolver()::resolve)
                .orElseThrow(() -> new IllegalStateException("Kotlin Continuation not present"));

        if (type.kind() == Kind.PARAMETERIZED_TYPE) {
            type = type.asParameterizedType().arguments().get(0);

            if (type.kind() == Kind.WILDCARD_TYPE) {
                Type extendsBound = type.asWildcardType().extendsBound();
                Type superBound = type.asWildcardType().superBound();
                type = superBound != null ? superBound : extendsBound;
            }
        }

        return type;
    }

    default Schema kotlinContinuationToSchema(AnnotationScannerContext context, MethodInfo method) {
        Type type = getKotlinContinuationArgument(context, method);
        AnnotationInstance schemaAnnotation = context.annotations().getMethodParameterAnnotation(method, type,
                SchemaConstant.DOTNAME_SCHEMA);
        return SchemaFactory.typeToSchema(context, type, schemaAnnotation, context.getExtensions());
    }

    /**
     * Determine if the default response information should be generated.
     * It should be done when no responses have been declared or if the default
     * response already exists and is missing information (e.g. content).
     *
     * @param status the status determined to be the generated default
     * @param operation current operation
     * @return true if a default response should be generated, otherwise false.
     */
    default boolean generateResponse(String status, Operation operation) {
        APIResponses responses = operation.getResponses();
        return responses == null || responses.getAPIResponse(status) != null;
    }

    /**
     * Add API response to API responses using the annotation information
     *
     * @param responseSchema
     *        The APIResponseSchema created from a
     *        {@link org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema
     *        APIResponseSchema} annotation
     * @param method
     *        the current method
     * @param operation
     *        the method operation
     */
    default void addApiReponseSchemaFromAnnotation(Map<String, APIResponse> responseSchema,
            MethodInfo method, Operation operation) {

        Map.Entry<String, APIResponse> entry = responseSchema.entrySet().iterator().next();
        String responseCode = entry.getKey();

        final int status;

        if (responseCode != null && responseCode.matches("\\d{3}")) {
            status = Integer.parseInt(responseCode);
        } else {
            status = getDefaultStatus(method);
            responseCode = String.valueOf(status);
        }

        APIResponse response = entry.getValue();

        if (response.getDescription() == null) {
            response.setDescription(getReasonPhrase(status));
        }

        ModelUtil.responses(operation).addAPIResponse(responseCode, response);
    }

    /**
     * Get the security requirements on method and class and add them to the openapi model
     *
     * @param resourceClass the class
     * @param method the method
     * @param operation the operation to add them to
     */
    default void processSecurityRequirementAnnotation(AnnotationScannerContext context, final ClassInfo resourceClass,
            final MethodInfo method,
            Operation operation) {

        List<SecurityRequirement> securityRequirements = context.io().securityIO().readRequirements(method);
        boolean emptyContainerPresent = isEmptySecurityRequirements(context, method);

        if (securityRequirements.isEmpty() && !emptyContainerPresent) {
            securityRequirements = context.io().securityIO().readRequirements(resourceClass);
            emptyContainerPresent = isEmptySecurityRequirements(context, resourceClass);
        }

        if (securityRequirements.isEmpty() && emptyContainerPresent) {
            operation.setSecurity(new ArrayList<>(0));
        } else {
            securityRequirements.forEach(operation::addSecurityRequirement);
        }
    }

    /**
     * Determines whether the target is annotated with an empty <code>@SecurityRequirements</code>
     * or <code>@SecurityRequirementsSets</code> annotation.
     *
     * @param target
     * @return true if an empty annotation is present, otherwise false
     */
    default boolean isEmptySecurityRequirements(AnnotationScannerContext context, AnnotationTarget target) {
        return Stream.of(Names.SECURITY_REQUIREMENTS, Names.SECURITY_REQUIREMENTS_SETS)
                .map(name -> context.annotations().getAnnotation(target, name))
                .filter(Objects::nonNull)
                .map(annotation -> context.annotations().<AnnotationInstance[]> value(annotation))
                .anyMatch(values -> values == null || values.length == 0);
    }

    /**
     * Process a callback annotation
     *
     * @param context the scanning context
     * @param method the method
     * @param operation the operation to add this to
     */
    default void processCallback(final AnnotationScannerContext context, final MethodInfo method, Operation operation) {
        Map<String, Callback> callbacks = context.io().callbackIO().readMap(method);

        if (!callbacks.isEmpty()) {
            operation.setCallbacks(callbacks);
        }
    }

    /**
     * Process a certain method for server annotations.
     *
     * @param method the method that contain the server annotation
     * @param operation the current Operation model being created
     */
    default void processServerAnnotation(final AnnotationScannerContext context, final MethodInfo method, Operation operation) {
        List<Server> servers = context.io().serverIO().readList(method);

        if (servers.isEmpty()) {
            servers = context.io().serverIO().readList(method.declaringClass());
        }

        servers.forEach(operation::addServer);
    }

    /**
     * Process the Extensions annotations
     *
     * @param context the scanning context
     * @param method the current REST method
     * @param operation the current operation
     */
    default void processExtensions(final AnnotationScannerContext context, final MethodInfo method, Operation operation) {
        Map<String, Object> methodExtensions = context.io().extensionIO().readMap(method);

        if (methodExtensions.isEmpty()) {
            context.io().extensionIO().readMap(method.declaringClass()).forEach(operation::addExtension);
        } else {
            methodExtensions.forEach(operation::addExtension);
        }
    }

    /**
     * Scan for scanner extensions
     *
     * @param context the scanning context
     * @param applications the scanner applications
     */
    default void processScannerExtensions(final AnnotationScannerContext context, Collection<ClassInfo> applications) {
        // this can be a useful extension point to set/override the application path
        for (AnnotationScannerExtension extension : context.getExtensions()) {
            extension.processScannerApplications(this, applications);
        }
    }

    /**
     * Process the request body
     *
     * @param context the current scanning context
     * @param method the resource method
     * @param params the params
     * @return RequestBody model
     */
    default RequestBody processRequestBody(final AnnotationScannerContext context,
            final MethodInfo method,
            final ResourceParameters params) {
        RequestBody requestBody = null;

        for (AnnotationInstance annotation : context.io().requestBodyIO().getRepeatableAnnotations(method)) {
            requestBody = context.io().requestBodyIO().read(annotation);
            Content formBodyContent = params.getFormBodyContent();

            if (formBodyContent != null) {
                // If form parameters were present, overlay RequestBody onto the generated form content
                requestBody.setContent(MergeUtil.mergeObjects(formBodyContent, requestBody.getContent()));
            }

            // TODO if the method argument type is Request, don't generate a Schema!

            Type requestBodyType = null;
            if (annotation.target().kind() == METHOD_PARAMETER) {
                requestBodyType = method.parameterType(annotation.target().asMethodParameter().position());
            } else if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
                requestBodyType = getRequestBodyParameterClassType(context, method, params);
            }

            // Only generate the request body schema if the @RequestBody is not a reference and no schema is yet specified
            if (requestBodyType != null && requestBody.getRef() == null) {
                Type[] views = context.annotations()
                        .value(context.annotations().getMethodParameterAnnotation(method, requestBodyType,
                                JacksonConstants.JSON_VIEW));
                setJsonViewContext(context, views);
                if (!ModelUtil.requestBodyHasSchema(requestBody)) {
                    requestBodyType = context.getResourceTypeResolver().resolve(requestBodyType);
                    AnnotationInstance schemaAnnotation = context.annotations().getMethodParameterAnnotation(method,
                            requestBodyType,
                            SchemaConstant.DOTNAME_SCHEMA);
                    Schema schema = SchemaFactory.typeToSchema(context, requestBodyType, schemaAnnotation,
                            context.getExtensions());

                    if (schema != null) {
                        ModelUtil.setRequestBodySchema(requestBody, schema, getConsumes(context));
                    }
                }

                if (!Extensions.getIsRequiredSet(requestBody) && TypeUtil.isOptional(requestBodyType)) {
                    requestBody.setRequired(Boolean.FALSE);
                }

                setRequestBodyConstraints(context, requestBody, method, requestBodyType);
            }
        }

        if (requestBody == null) {
            requestBody = context.io().requestBodyIO().readRequestSchema(method);
        }

        // If the request body is null, figure it out from the parameters.  Only if the
        // method declares that it @Consumes data
        if ((requestBody == null || (requestBody.getContent() == null && requestBody.getRef() == null))
                && getConsumes(context) != null) {
            if (params.getFormBodyContent() != null) {
                if (requestBody == null) {
                    requestBody = OASFactory.createRequestBody();
                    Extensions.setRequiredDefault(requestBody, Boolean.TRUE);
                }
                requestBody.setContent(params.getFormBodyContent());
            } else {
                Type requestBodyType = getRequestBodyParameterClassType(context, method, params);
                requestBodyType = context.getResourceTypeResolver().resolve(requestBodyType);

                if (requestBodyType != null && !isScannerInternalParameter(requestBodyType)) {
                    Type[] views = context.annotations().value(
                            context.annotations().getMethodParameterAnnotation(method, requestBodyType,
                                    JacksonConstants.JSON_VIEW));
                    setJsonViewContext(context, views);
                    Schema schema = null;

                    if (isMultipartInput(requestBodyType)) {
                        schema = OASFactory.createSchema();
                        schema.addType(Schema.SchemaType.OBJECT);
                    } else {
                        AnnotationInstance schemaAnnotation = context.annotations().getMethodParameterAnnotation(method,
                                requestBodyType,
                                SchemaConstant.DOTNAME_SCHEMA);
                        schema = SchemaFactory.typeToSchema(context, requestBodyType, schemaAnnotation,
                                context.getExtensions());
                    }

                    if (requestBody == null) {
                        requestBody = OASFactory.createRequestBody();
                        Extensions.setRequiredDefault(requestBody, Boolean.TRUE);
                    }

                    if (schema != null) {
                        ModelUtil.setRequestBodySchema(requestBody, schema, getConsumesForRequestBody(context));
                    }

                    if (!Extensions.getIsRequiredSet(requestBody) && TypeUtil.isOptional(requestBodyType)) {
                        requestBody.setRequired(Boolean.FALSE);
                    }

                    setRequestBodyConstraints(context, requestBody, method, requestBodyType);
                }
            }
        }

        clearJsonViewContext(context);

        return requestBody;
    }

    default String[] getConsumes(final AnnotationScannerContext context) {
        String[] currentConsumes = context.getCurrentConsumes();
        if (currentConsumes == null || currentConsumes.length == 0) {
            currentConsumes = context.getConfig().getDefaultConsumes().orElse(null);
        }
        return currentConsumes;
    }

    default String[] getConsumesForRequestBody(final AnnotationScannerContext context) {
        String[] currentConsumes = context.getCurrentConsumes();
        if (currentConsumes == null || currentConsumes.length == 0) {
            currentConsumes = context.getDefaultConsumes();
        }
        return currentConsumes;
    }

    /**
     * Go through the method parameters looking for one that is not a Kotlin Continuation,
     * is not annotated with a jax-rs/spring annotation, and is not a known path parameter.
     * That will be the one that is the request body.
     *
     * @param context the scanning context
     * @param method MethodInfo
     * @param params the current parameters
     * @return Type
     */
    default Type getRequestBodyParameterClassType(final AnnotationScannerContext context, MethodInfo method,
            final ResourceParameters params) {

        List<Type> methodParams = method.parameterTypes();

        return IntStream.range(0, methodParams.size())
                .filter(position -> !isKotlinContinuation(methodParams.get(position)))
                .filter(position -> !isFrameworkContextType(methodParams.get(position)))
                .filter(position -> !isPathParameter(context, method.parameterName(position), params))
                .filter(position -> {
                    List<AnnotationInstance> annotations = context.annotations().getMethodParameterAnnotations(method,
                            position);
                    return annotations.isEmpty() || !containsScannerAnnotations(annotations, context.getExtensions());
                })
                .mapToObj(methodParams::get)
                .findFirst()
                .orElse(null);
    }

    default void setRequestBodyConstraints(AnnotationScannerContext context, RequestBody requestBody, MethodInfo method,
            Type requestBodyType) {
        List<AnnotationInstance> paramAnnotations = context.annotations().getMethodParameterAnnotations(method,
                requestBodyType);
        Optional<BeanValidationScanner> constraintScanner = context.getBeanValidationScanner();

        if (!paramAnnotations.isEmpty() && constraintScanner.isPresent()) {
            AnnotationTarget paramTarget = paramAnnotations.iterator().next().target();

            Optional.ofNullable(requestBody.getContent())
                    .map(Content::getMediaTypes)
                    .map(Map::entrySet)
                    .orElseGet(Collections::emptySet)
                    .stream()
                    .map(Map.Entry::getValue)
                    .map(MediaType::getSchema)
                    .filter(Objects::nonNull)
                    .forEach(schema -> constraintScanner.get().applyConstraints(paramTarget, schema, null,
                            (target, name) -> {
                                if (!Extensions.getIsRequiredSet(requestBody)) {
                                    requestBody.setRequired(Boolean.TRUE);
                                }
                            }));
        }
    }

    default boolean isPathParameter(final AnnotationScannerContext context, String name, final ResourceParameters params) {
        if (context.getConfig().allowNakedPathParameter().orElse(Boolean.FALSE)) {
            return params.getAllParameters()
                    .stream()
                    .map(p -> ModelUtil.dereference(context.getOpenApi(), p))
                    .filter(p -> Objects.equals(p.getName(), name))
                    .anyMatch(p -> Objects.equals(p.getIn(), Parameter.In.PATH));
        }
        return false;
    }

    /**
     * Determines whether the given type is a special "context" type of the current
     * web/REST framework and should not be considered as a request body type.
     *
     * This method should be overridden by framework-specific annotation scanners.
     *
     * @param type the type to consider
     *
     * @return true if the type is a framework-specific special/context type, otherwise false.
     */
    default boolean isFrameworkContextType(Type type) {
        return false;
    }

    /**
     * Get the default description for a HTTP Status code
     *
     * @param statusCode
     * @return the reason
     */
    default String getReasonPhrase(int statusCode) {
        switch (statusCode) {
            case 100:
                return "Continue";
            case 101:
                return "Switching Protocols";
            case 102:
                return "Processing";
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 202:
                return "Accepted";
            case 203:
                return "Non-authoritative Information";
            case 204:
                return "No Content";
            case 205:
                return "Reset Content";
            case 206:
                return "Partial Content";
            case 207:
                return "Multi-Status";
            case 208:
                return "Already Reported";
            case 226:
                return "IM Used";
            case 300:
                return "Multiple Choices";
            case 301:
                return "Moved Permanently";
            case 302:
                return "Found";
            case 303:
                return "See Other";
            case 304:
                return "Not Modified";
            case 305:
                return "Use Proxy";
            case 307:
                return "Temporary Redirect";
            case 308:
                return "Permanent Redirect";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 402:
                return "Payment Required";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 405:
                return "Method Not Allowed";
            case 406:
                return "Not Acceptable";
            case 407:
                return "Proxy Authentication Required";
            case 408:
                return "Request Timeout";
            case 409:
                return "Conflict";
            case 410:
                return "Gone";
            case 411:
                return "Length Required";
            case 412:
                return "Precondition Failed";
            case 413:
                return "Payload Too Large";
            case 414:
                return "Request-URI Too Long";
            case 415:
                return "Unsupported Media Type";
            case 416:
                return "Requested Range Not Satisfiable";
            case 417:
                return "Expectation Failed";
            case 418:
                return "I'm a teapot";
            case 421:
                return "Misdirected Request";
            case 422:
                return "Unprocessable Entity";
            case 423:
                return "Locked";
            case 424:
                return "Failed Dependency";
            case 426:
                return "Upgrade Required";
            case 428:
                return "Precondition Required";
            case 429:
                return "Too Many Requests";
            case 431:
                return "Request Header Fields Too Large";
            case 444:
                return "Connection Closed Without Response";
            case 451:
                return "Unavailable For Legal Reasons";
            case 499:
                return "Client Closed Request";
            case 500:
                return "Internal Server Error";
            case 501:
                return "Not Implemented";
            case 502:
                return "Bad Gateway";
            case 503:
                return "Service Unavailable";
            case 504:
                return "Gateway Timeout";
            case 505:
                return "HTTP Version Not Supported";
            case 506:
                return "Variant Also Negotiates";
            case 507:
                return "Insufficient Storage";
            case 508:
                return "Loop Detected";
            case 510:
                return "Not Extended";
            case 511:
                return "Network Authentication Required";
            case 599:
                return "Network Connect Timeout Error";
            default:
                return "Unknown";
        }
    }
}
