package io.smallrye.openapi.runtime.scanner.spi;

import static org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

import io.smallrye.openapi.api.OpenApiConfig.OperationIdStrategy;
import io.smallrye.openapi.api.constants.KotlinConstants;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.constants.SecurityConstants;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.parameters.RequestBodyImpl;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.CurrentScannerInfo;
import io.smallrye.openapi.runtime.io.callback.CallbackReader;
import io.smallrye.openapi.runtime.io.definition.DefinitionReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.operation.OperationReader;
import io.smallrye.openapi.runtime.io.requestbody.RequestBodyReader;
import io.smallrye.openapi.runtime.io.response.ResponseReader;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.io.securityrequirement.SecurityRequirementReader;
import io.smallrye.openapi.runtime.io.securityscheme.SecuritySchemeReader;
import io.smallrye.openapi.runtime.io.server.ServerReader;
import io.smallrye.openapi.runtime.io.tag.TagConstant;
import io.smallrye.openapi.runtime.io.tag.TagReader;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.processor.JavaSecurityProcessor;
import io.smallrye.openapi.runtime.util.JandexUtil;
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
     * @param openApi the current OpenApi model being created
     */
    default void processDefinitionAnnotation(final AnnotationScannerContext context, final ClassInfo targetClass,
            OpenAPI openApi) {
        AnnotationInstance openApiDefAnno = DefinitionReader.getDefinitionAnnotation(targetClass);
        if (openApiDefAnno != null) {
            DefinitionReader.processDefinition(context, openApi, openApiDefAnno);
        }
    }

    /**
     * Process a certain class for security annotations.
     * 
     * @param targetClass the class that contain the security annotation
     * @param openApi the current OpenApi model being created
     */
    default void processSecuritySchemeAnnotation(final AnnotationScannerContext context, final ClassInfo targetClass,
            OpenAPI openApi) {
        List<AnnotationInstance> securitySchemeAnnotations = SecuritySchemeReader.getSecuritySchemeAnnotations(targetClass);

        for (AnnotationInstance annotation : securitySchemeAnnotations) {
            String name = SecuritySchemeReader.getSecuritySchemeName(annotation);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                SecurityScheme securityScheme = SecuritySchemeReader.readSecurityScheme(context, annotation);
                Components components = ModelUtil.components(openApi);
                components.addSecurityScheme(name, securityScheme);
            }
        }
    }

    /**
     * Process a certain class for server annotations.
     * 
     * @param targetClass the class that contain the server annotation
     * @param openApi the current OpenApi model being created
     */
    default void processServerAnnotation(final ClassInfo targetClass, OpenAPI openApi) {
        List<AnnotationInstance> serverAnnotations = ServerReader.getServerAnnotations(targetClass);
        for (AnnotationInstance annotation : serverAnnotations) {
            Server server = ServerReader.readServer(annotation);
            openApi.addServer(server);
        }
    }

    /**
     * Process Java security (roles allowed and declared roles)
     * 
     * @param openApi the OpenAPI Model
     * @param resourceClass the Class being scanned
     */
    default void processJavaSecurity(ClassInfo resourceClass, OpenAPI openApi) {
        JavaSecurityProcessor.register(openApi);
        JavaSecurityProcessor
                .addDeclaredRolesToScopes(TypeUtil.getAnnotationValue(resourceClass, SecurityConstants.DECLARE_ROLES));
        JavaSecurityProcessor
                .addRolesAllowedToScopes(TypeUtil.getAnnotationValue(resourceClass, SecurityConstants.ROLES_ALLOWED));
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
        if (!TagReader.hasTagAnnotation(target)) {
            return nullWhenMissing ? null : Collections.emptySet();
        }

        Set<String> tags = new LinkedHashSet<>();
        List<AnnotationInstance> tagAnnos = TagReader.getTagAnnotations(target);

        for (AnnotationInstance ta : tagAnnos) {
            if (JandexUtil.isRef(ta)) {
                tags.add(JandexUtil.value(ta, OpenApiConstants.REF));
            } else {
                Tag tag = TagReader.readTag(context, ta);

                if (tag.getName() != null) {
                    ModelUtil.addTag(openApi, tag);
                    tags.add(tag.getName());
                }
            }
        }

        String[] refs = TypeUtil.getAnnotationValue(target, TagConstant.DOTNAME_TAGS,
                OpenApiConstants.REFS);

        if (refs != null) {
            Arrays.stream(refs).forEach(tags::add);
        }

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
        Map<ClassInfo, Type> chain = JandexUtil.inheritanceChain(context.getIndex(), resource, resourceType);
        List<MethodInfo> methods = new ArrayList<>();

        for (ClassInfo classInfo : chain.keySet()) {
            classInfo.methods()
                    .stream()
                    .filter(method -> !method.isSynthetic())
                    .forEach(methods::add);

            classInfo.interfaceTypes()
                    .stream()
                    .map(iface -> context.getIndex().getClassByName(TypeUtil.getName(iface)))
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
        if (OperationReader.operationIsHidden(method)) {
            return Optional.empty();
        }
        AnnotationInstance operationAnnotation = OperationReader.getOperationAnnotation(method);
        OperationImpl operation = (OperationImpl) OperationReader.readOperation(context, operationAnnotation, method);

        if (operation == null) {
            operation = new OperationImpl();
        }

        operation.setMethodRef(JandexUtil.createUniqueMethodReference(resourceClass, method));

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

        return Optional.of(operation);
    }

    default void processResponse(final AnnotationScannerContext context, final MethodInfo method, Operation operation,
            Map<DotName, AnnotationInstance> exceptionAnnotationMap) {

        List<AnnotationInstance> apiResponseAnnotations = ResponseReader.getResponseAnnotations(method);
        for (AnnotationInstance annotation : apiResponseAnnotations) {
            addApiReponseFromAnnotation(context, annotation, operation);
        }

        AnnotationInstance responseSchemaAnnotation = ResponseReader.getResponseSchemaAnnotation(method);
        addApiReponseSchemaFromAnnotation(context, responseSchemaAnnotation, method, operation);

        /*
         * If there is no response from annotations, try to create one from the method return value.
         * Do not generate a response if the app has used an empty @ApiResponses annotation. This
         * provides a way for the application to indicate that responses will be supplied some other
         * way (i.e. static file).
         */
        AnnotationInstance apiResponses = ResponseReader.getResponsesAnnotation(method);
        if (apiResponses == null || !JandexUtil.isEmpty(apiResponses)) {
            createResponseFromRestMethod(context, method, operation);
        }

        //Add api response using list of exceptions in the methods and exception mappers
        List<Type> methodExceptions = method.exceptions();

        for (Type type : methodExceptions) {
            DotName exceptionDotName = type.name();
            if (exceptionAnnotationMap != null && !exceptionAnnotationMap.isEmpty()
                    && exceptionAnnotationMap.keySet().contains(exceptionDotName)) {
                AnnotationInstance exMapperApiResponseAnnotation = exceptionAnnotationMap.get(exceptionDotName);
                if (!this.responseCodeExistInMethodAnnotations(context, exMapperApiResponseAnnotation,
                        apiResponseAnnotations)) {
                    addApiReponseFromAnnotation(context, exMapperApiResponseAnnotation, operation);
                }
            }
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
     * TODO: generate responses for each checked exception?
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
                response = new APIResponseImpl().description(description);
            }
        } else if (generateResponse(code, operation)) {
            response = new APIResponseImpl().description(description);

            /*
             * Only generate content if not already supplied in annotations and the
             * method does not return an opaque Scanner Response
             */
            if (!isScannerInternalResponse(returnType, context, method) &&
                    (ModelUtil.responses(operation).getAPIResponse(code) == null ||
                            ModelUtil.responses(operation).getAPIResponse(code).getContent() == null)) {

                Schema schema;

                if (isMultipartOutput(returnType)) {
                    schema = new SchemaImpl().type(Schema.SchemaType.OBJECT);
                } else if (hasKotlinContinuation(method)) {
                    schema = kotlinContinuationToSchema(context, method);
                } else {
                    schema = SchemaFactory.typeToSchema(context, returnType, context.getExtensions());
                }

                Content content = new ContentImpl();
                String[] produces = CurrentScannerInfo.getCurrentProduces();

                if (produces == null || produces.length == 0) {
                    produces = context.getConfig().getDefaultProduces().orElse(OpenApiConstants.DEFAULT_MEDIA_TYPES.get());
                }

                if (schema != null && schema.getNullable() == null && TypeUtil.isOptional(returnType)) {
                    schema.setNullable(Boolean.TRUE);
                }

                for (String producesType : produces) {
                    MediaType mt = new MediaTypeImpl();
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
        final Type returnType = method.returnType();

        if (returnType.kind().equals(Type.Kind.VOID)) {
            return true;
        }

        if (isWrapperType(returnType)) {
            ParameterizedType parameterizedType = returnType.asParameterizedType();
            return parameterizedType.arguments().stream().anyMatch(TypeUtil::isVoid);
        } else if (TypeUtil.isWrappedType(returnType)) {
            return TypeUtil.isVoid(TypeUtil.unwrapType(returnType));
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
        return method.parameters().stream().anyMatch(this::isKotlinContinuation);
    }

    default boolean isKotlinContinuation(Type paramType) {
        return KotlinConstants.CONTINUATION.equals(paramType.name());
    }

    default Type getKotlinContinuationArgument(AnnotationScannerContext context, MethodInfo method) {
        Type type = method.parameters()
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
        return SchemaFactory.typeToSchema(context, type, context.getExtensions());
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
     * Add api response to api responses using the annotation information
     *
     * @param context The current scanning context
     * @param apiResponseAnnotation The api response annotation
     * @param operation The method operation
     */
    default void addApiReponseFromAnnotation(final AnnotationScannerContext context, AnnotationInstance apiResponseAnnotation,
            Operation operation) {
        String responseCode = ResponseReader.getResponseName(context, apiResponseAnnotation);
        if (responseCode == null) {
            responseCode = APIResponses.DEFAULT;
        }
        APIResponse response = ResponseReader.readResponse(context, apiResponseAnnotation);
        APIResponses responses = ModelUtil.responses(operation);
        responses.addAPIResponse(responseCode, response);
    }

    /**
     * Add api response to api responses using the annotation information
     * 
     * @param context the scanning context
     * @param annotation The APIResponseSchema annotation
     * @param method the current method
     * @param operation the method operation
     */
    default void addApiReponseSchemaFromAnnotation(AnnotationScannerContext context,
            AnnotationInstance annotation,
            MethodInfo method,
            Operation operation) {

        if (annotation == null) {
            return;
        }

        String responseCode = ResponseReader.getResponseName(context, annotation);
        final int status;

        if (responseCode != null && responseCode.matches("\\d{3}")) {
            status = Integer.parseInt(responseCode);
        } else {
            status = getDefaultStatus(method);
            responseCode = String.valueOf(status);
        }

        APIResponse response = ResponseReader.readResponseSchema(context, annotation);

        if (response.getDescription() == null) {
            response.setDescription(getReasonPhrase(status));
        }

        APIResponses responses = ModelUtil.responses(operation);
        responses.addAPIResponse(responseCode, response);
    }

    /**
     * Check if the response code declared in the ExceptionMapper already defined in one of the ApiReponse annotations of the
     * method.
     * If the response code already exists then ignore the exception mapper annotation.
     *
     * @param exMapperApiResponseAnnotation ApiResponse annotation declared in the exception mapper
     * @param methodApiResponseAnnotations List of ApiResponse annotations declared in the jax-rs/spring method.
     * @return response code exist or not
     */
    default boolean responseCodeExistInMethodAnnotations(AnnotationScannerContext context,
            AnnotationInstance exMapperApiResponseAnnotation,
            List<AnnotationInstance> methodApiResponseAnnotations) {

        String exMapperResponseCode = ResponseReader.getResponseName(context, exMapperApiResponseAnnotation);

        return methodApiResponseAnnotations.stream()
                .map(a -> ResponseReader.getResponseName(context, a))
                .filter(Objects::nonNull)
                .anyMatch(code -> code.equals(exMapperResponseCode));
    }

    /**
     * Get the security requirements on method and class and add them to the openapi model
     * 
     * @param resourceClass the class
     * @param method the method
     * @param operation the operation to add them to
     */
    default void processSecurityRequirementAnnotation(final ClassInfo resourceClass, final MethodInfo method,
            Operation operation) {

        List<AnnotationInstance> requirements;

        if (isEmptySecurityRequirements(method)) {
            operation.setSecurity(new ArrayList<>(0));
        } else {
            requirements = SecurityRequirementReader.getSecurityRequirementAnnotations(method);

            if (requirements.isEmpty()) {
                if (isEmptySecurityRequirements(resourceClass)) {
                    operation.setSecurity(new ArrayList<>(0));
                } else {
                    requirements = SecurityRequirementReader.getSecurityRequirementAnnotations(resourceClass);
                }
            }

            for (AnnotationInstance annotation : requirements) {
                SecurityRequirement requirement = SecurityRequirementReader.readSecurityRequirement(annotation);
                if (requirement != null) {
                    operation.addSecurityRequirement(requirement);
                }
            }
        }
    }

    /**
     * Determines whether the target is annotated with an empty <code>@SecurityRequirements</code>
     * annotation.
     * 
     * @param target
     * @return true if an empty annotation is present, otherwise false
     */
    default boolean isEmptySecurityRequirements(AnnotationTarget target) {
        AnnotationInstance securityRequirements = SecurityRequirementReader.getSecurityRequirementsAnnotation(target);

        if (securityRequirements != null) {
            AnnotationInstance[] values = JandexUtil.value(securityRequirements, "value");
            if (values == null || values.length == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Process a callback annotation
     * 
     * @param context the scanning context
     * @param method the method
     * @param operation the operation to add this to
     */
    default void processCallback(final AnnotationScannerContext context, final MethodInfo method, Operation operation) {
        List<AnnotationInstance> callbackAnnotations = CallbackReader.getCallbackAnnotations(method);

        Map<String, Callback> callbacks = new LinkedHashMap<>();
        for (AnnotationInstance annotation : callbackAnnotations) {
            String name = CallbackReader.getCallbackName(annotation);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                callbacks.put(name, CallbackReader.readCallback(context, annotation));
            }

            if (!callbacks.isEmpty()) {
                operation.setCallbacks(callbacks);
            }
        }
    }

    /**
     * Process a certain method for server annotations.
     * 
     * @param method the method that contain the server annotation
     * @param operation the current Operation model being created
     */
    default void processServerAnnotation(final MethodInfo method, Operation operation) {
        List<AnnotationInstance> serverAnnotations = ServerReader.getServerAnnotations(method);
        if (serverAnnotations.isEmpty()) {
            serverAnnotations.addAll(ServerReader.getServerAnnotations(method.declaringClass()));
        }
        for (AnnotationInstance annotation : serverAnnotations) {
            Server server = ServerReader.readServer(annotation);
            operation.addServer(server);
        }
    }

    /**
     * Process the Extensions annotations
     * 
     * @param context the scanning context
     * @param method the current REST method
     * @param operation the current operation
     */
    default void processExtensions(final AnnotationScannerContext context, final MethodInfo method, Operation operation) {
        List<AnnotationInstance> extensionAnnotations = ExtensionReader.getExtensionsAnnotations(method);

        if (extensionAnnotations.isEmpty()) {
            extensionAnnotations.addAll(ExtensionReader.getExtensionsAnnotations(method.declaringClass()));
        }
        for (AnnotationInstance annotation : extensionAnnotations) {
            if (annotation.target() == null || !METHOD_PARAMETER.equals(annotation.target().kind())) {
                String name = ExtensionReader.getExtensionName(annotation);
                operation.addExtension(name, ExtensionReader.readExtensionValue(context, name, annotation));
            }
        }
    }

    /**
     * Set the created operation to the pathItem
     * 
     * @param methodType the HTTP method type
     * @param pathItem the pathItem to set
     * @param operation the operation
     */
    default void setOperationOnPathItem(PathItem.HttpMethod methodType, PathItem pathItem, Operation operation) {
        switch (methodType) {
            case DELETE:
                pathItem.setDELETE(operation);
                break;
            case GET:
                pathItem.setGET(operation);
                break;
            case HEAD:
                pathItem.setHEAD(operation);
                break;
            case OPTIONS:
                pathItem.setOPTIONS(operation);
                break;
            case PATCH:
                pathItem.setPATCH(operation);
                break;
            case POST:
                pathItem.setPOST(operation);
                break;
            case PUT:
                pathItem.setPUT(operation);
                break;
            case TRACE:
                pathItem.setTRACE(operation);
                break;
            default:
                break;
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

        List<AnnotationInstance> requestBodyAnnotations = RequestBodyReader.getRequestBodyAnnotations(method);
        for (AnnotationInstance annotation : requestBodyAnnotations) {
            requestBody = RequestBodyReader.readRequestBody(context, annotation);
            Content formBodyContent = params.getFormBodyContent();

            if (formBodyContent != null) {
                // If form parameters were present, overlay RequestBody onto the generated form content
                requestBody.setContent(MergeUtil.mergeObjects(formBodyContent, requestBody.getContent()));
            }

            // TODO if the method argument type is Request, don't generate a Schema!

            Type requestBodyType = null;
            if (annotation.target().kind() == METHOD_PARAMETER) {
                requestBodyType = JandexUtil.getMethodParameterType(method,
                        annotation.target().asMethodParameter().position());
            } else if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
                requestBodyType = getRequestBodyParameterClassType(context, method, params);
            }

            // Only generate the request body schema if the @RequestBody is not a reference and no schema is yet specified
            if (requestBodyType != null && requestBody.getRef() == null) {
                if (!ModelUtil.requestBodyHasSchema(requestBody)) {
                    requestBodyType = context.getResourceTypeResolver().resolve(requestBodyType);
                    Schema schema = SchemaFactory.typeToSchema(context, requestBodyType, context.getExtensions());

                    if (schema != null) {
                        ModelUtil.setRequestBodySchema(requestBody, schema, getConsumes(context));
                    }
                }

                if (requestBody.getRequired() == null && TypeUtil.isOptional(requestBodyType)) {
                    requestBody.setRequired(Boolean.FALSE);
                }
            }
        }

        if (requestBody == null) {
            requestBody = RequestBodyReader.readRequestBodySchema(context,
                    RequestBodyReader.getRequestBodySchemaAnnotation(method));
        }

        // If the request body is null, figure it out from the parameters.  Only if the
        // method declares that it @Consumes data
        if ((requestBody == null || (requestBody.getContent() == null && requestBody.getRef() == null))
                && getConsumes(context) != null) {
            if (params.getFormBodyContent() != null) {
                if (requestBody == null) {
                    requestBody = new RequestBodyImpl();
                }
                requestBody.setContent(params.getFormBodyContent());
            } else {
                Type requestBodyType = getRequestBodyParameterClassType(context, method, params);
                requestBodyType = context.getResourceTypeResolver().resolve(requestBodyType);

                if (requestBodyType != null && !isScannerInternalParameter(requestBodyType)) {
                    Schema schema = null;

                    if (isMultipartInput(requestBodyType)) {
                        schema = new SchemaImpl();
                        schema.setType(Schema.SchemaType.OBJECT);
                    } else {
                        schema = SchemaFactory.typeToSchema(context, requestBodyType, context.getExtensions());
                    }

                    if (requestBody == null) {
                        requestBody = new RequestBodyImpl();
                    }

                    if (schema != null) {
                        ModelUtil.setRequestBodySchema(requestBody, schema, getConsumes(context));
                    }

                    if (requestBody.getRequired() == null && TypeUtil.isOptional(requestBodyType)) {
                        requestBody.setRequired(Boolean.FALSE);
                    }
                }
            }
        }
        return requestBody;
    }

    default String[] getConsumes(final AnnotationScannerContext context) {
        String[] currentConsumes = CurrentScannerInfo.getCurrentConsumes();
        if (currentConsumes == null || currentConsumes.length == 0) {
            currentConsumes = context.getConfig().getDefaultConsumes().orElse(null);
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

        List<Type> methodParams = method.parameters();

        return IntStream.range(0, methodParams.size())
                .filter(position -> !isKotlinContinuation(methodParams.get(position)))
                .filter(position -> !isPathParameter(context, method.parameterName(position), params))
                .filter(position -> {
                    List<AnnotationInstance> annotations = JandexUtil.getParameterAnnotations(method, (short) position);
                    return annotations.isEmpty() || !containsScannerAnnotations(annotations, context.getExtensions());
                })
                .mapToObj(methodParams::get)
                .findFirst()
                .orElse(null);
    }

    default boolean isPathParameter(final AnnotationScannerContext context, String name, final ResourceParameters params) {
        if (context.getConfig().allowNakedPathParameter().orElse(Boolean.FALSE)) {
            List<Parameter> allParameters = params.getAllParameters();
            for (Parameter p : allParameters) {
                if (p.getIn().equals(Parameter.In.PATH) && p.getName().equals(name)) {
                    return true;
                }
            }
        }
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
