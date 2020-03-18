package io.smallrye.openapi.jaxrs;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
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
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.constants.SecurityConstants;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.api.models.PathsImpl;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.parameters.RequestBodyImpl;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import io.smallrye.openapi.api.util.ListUtil;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.CurrentScannerInfo;
import io.smallrye.openapi.runtime.io.callback.CallbackReader;
import io.smallrye.openapi.runtime.io.definition.DefinitionReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.operation.OperationReader;
import io.smallrye.openapi.runtime.io.parameter.ParameterReader;
import io.smallrye.openapi.runtime.io.requestbody.RequestBodyReader;
import io.smallrye.openapi.runtime.io.response.ResponseConstant;
import io.smallrye.openapi.runtime.io.response.ResponseReader;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.io.securityrequirement.SecurityRequirementReader;
import io.smallrye.openapi.runtime.io.securityscheme.SecuritySchemeReader;
import io.smallrye.openapi.runtime.io.server.ServerReader;
import io.smallrye.openapi.runtime.io.tag.TagConstant;
import io.smallrye.openapi.runtime.io.tag.TagReader;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.PathMaker;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Scanner that scan Jax-Rs entry points.
 * This is also the default, as it's part of the spec.
 * 
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class JaxRsAnnotationScanner implements AnnotationScanner {
    private static final Logger LOG = Logger.getLogger(JaxRsAnnotationScanner.class);
    private static final String JAXRS_PACKAGE = "javax.ws.rs";
    private String currentAppPath = "";

    @Override
    public String getName() {
        return "JAX-RS";
    }

    @Override
    public boolean shouldIntrospectClassToSchema(ClassType classType) {
        return !classType.name().equals(JaxRsConstants.RESPONSE);
    }

    @Override
    public OpenAPI scan(final AnnotationScannerContext context, OpenAPI openApi) {
        // Get all jax-rs applications and convert them to OpenAPI models (and merge them into a single one)
        processApplicationClasses(context, openApi);

        // This needs to be here just after we have done JaxRs Application
        boolean tagsDefined = openApi.getTags() != null && !openApi.getTags().isEmpty();

        // Now find all jax-rs endpoints
        processResourceClasses(context, openApi);

        // Sort the tags unless the application has defined the order in OpenAPIDefinition annotation(s)
        sortTags(openApi, tagsDefined);

        // Now that all paths have been created, sort them (we don't have a better way to organize them).
        sortPaths(openApi);

        return openApi;
    }

    private void processApplicationClasses(final AnnotationScannerContext context, OpenAPI openApi) {
        // Get all jax-rs applications and convert them to OpenAPI models (and merge them into a single one)
        Collection<ClassInfo> applications = context.getIndex().getAllKnownSubclasses(JaxRsConstants.APPLICATION);
        for (ClassInfo classInfo : applications) {
            OpenAPI applicationOpenApi = processApplicationClass(context, classInfo);
            openApi = MergeUtil.merge(openApi, applicationOpenApi);
        }

        // this can be a useful extension point to set/override the application path
        processScannerExtensions(context, applications);
    }

    /**
     * Processes a JAX-RS {@link Application} and creates an {@link OpenAPI} model. Performs
     * annotation scanning and other processing. Returns a model unique to that single JAX-RS
     * app.
     * 
     * @param applicationClass
     */
    private OpenAPI processApplicationClass(final AnnotationScannerContext context, ClassInfo applicationClass) {
        OpenAPI openApi = new OpenAPIImpl();
        openApi.setOpenapi(OpenApiConstants.OPEN_API_VERSION);

        // Get the @ApplicationPath info and save it for later (also support @Path which seems nonstandard but common).
        AnnotationInstance appPathAnno = JandexUtil.getClassAnnotation(applicationClass,
                JaxRsConstants.APPLICATION_PATH);
        if (appPathAnno == null || context.getConfig().applicationPathDisable()) {
            appPathAnno = JandexUtil.getClassAnnotation(applicationClass, JaxRsConstants.PATH);
        }
        // TODO: Add support for Application selection when there are more than one
        if (appPathAnno != null) {
            this.currentAppPath = appPathAnno.value().asString();
        } else {
            this.currentAppPath = "/";
        }

        // Process @OpenAPIDefinition annotation
        processDefinitionAnnotation(context, applicationClass, openApi);

        // Process @SecurityScheme annotations
        processSecuritySchemeAnnotation(applicationClass, openApi);

        // Process @Server annotations
        processServerAnnotation(applicationClass, openApi);

        return openApi;
    }

    private void processScannerExtensions(final AnnotationScannerContext context, Collection<ClassInfo> applications) {
        // this can be a useful extension point to set/override the application path
        for (AnnotationScannerExtension extension : context.getExtensions()) {
            extension.processScannerApplications(this, applications);
        }
    }

    private void processResourceClasses(final AnnotationScannerContext context, OpenAPI openApi) {
        // Now find all jax-rs endpoints
        Collection<ClassInfo> resourceClasses = getJaxRsResourceClasses(context.getIndex());
        for (ClassInfo resourceClass : resourceClasses) {
            processResourceClass(context, openApi, resourceClass, null);
        }
    }

    /**
     * Processing a single JAX-RS resource class (annotated with @Path).
     * 
     * @param openApi
     * @param resourceClass
     * @param locatorPathParameters
     */
    private void processResourceClass(final AnnotationScannerContext context,
            OpenAPI openApi,
            ClassInfo resourceClass,
            List<Parameter> locatorPathParameters) {
        LOG.debug("Processing a JAX-RS resource class: " + resourceClass.simpleName());

        // Process @SecurityScheme annotations. TODO: Not jax-rs spesific
        processSecuritySchemeAnnotation(resourceClass, openApi);

        // Process roles allowed and declared roles. TODO: Not jax-rs spesific
        JavaSecurityProcessor.register(openApi);
        JavaSecurityProcessor
                .addDeclaredRolesToScopes(TypeUtil.getAnnotationValue(resourceClass, SecurityConstants.DECLARE_ROLES));
        JavaSecurityProcessor
                .addRolesAllowedToScopes(TypeUtil.getAnnotationValue(resourceClass, SecurityConstants.ROLES_ALLOWED));

        // From here it's JAX-RS specific (I think)

        // Now find and process the operation methods
        processResourceMethods(context, resourceClass, openApi, locatorPathParameters);

    }

    /**
     * Process the JAX-RS Operation methods
     * 
     * @param context the scanning context
     * @param resourceClass the class containing the methods
     * @param openApi the OpenApi model being processed
     * @param locatorPathParameters path parameters
     */
    private void processResourceMethods(final AnnotationScannerContext context,
            final ClassInfo resourceClass,
            OpenAPI openApi,
            List<Parameter> locatorPathParameters) {

        // Process tags (both declarations and references). TODO: Not jax-rs spesific
        Set<String> tagRefs = processTags(resourceClass, openApi, false);

        // Process exception mapper to auto generate api response based on method exceptions
        Map<DotName, AnnotationInstance> exceptionAnnotationMap = processExceptionMappers(context);

        for (MethodInfo methodInfo : getResourceMethods(context, resourceClass)) {
            final AtomicInteger resourceCount = new AtomicInteger(0);

            JaxRsConstants.HTTP_METHODS
                    .stream()
                    .filter(methodInfo::hasAnnotation)
                    .map(DotName::withoutPackagePrefix)
                    .map(PathItem.HttpMethod::valueOf)
                    .forEach(httpMethod -> {
                        resourceCount.incrementAndGet();
                        processResourceMethod(context, resourceClass, methodInfo, httpMethod, openApi, tagRefs,
                                locatorPathParameters, exceptionAnnotationMap);
                    });

            if (resourceCount.get() == 0 && methodInfo.hasAnnotation(JaxRsConstants.PATH)) {
                processSubResource(context, resourceClass, methodInfo, openApi, locatorPathParameters);
            }
        }
    }

    /**
     * Build a map between exception class name and its corresponding @ApiResponse annotation in the jax-rs exception mapper
     * 
     */
    private Map<DotName, AnnotationInstance> processExceptionMappers(final AnnotationScannerContext context) {
        Map<DotName, AnnotationInstance> exceptionHandlerMap = new HashMap<>();
        Collection<ClassInfo> exceptionMappers = context.getIndex()
                .getKnownDirectImplementors(JaxRsConstants.EXCEPTION_MAPPER);

        for (ClassInfo classInfo : exceptionMappers) {
            DotName exceptionDotName = classInfo.interfaceTypes()
                    .stream()
                    .filter(it -> it.name().equals(JaxRsConstants.EXCEPTION_MAPPER))
                    .filter(it -> it.kind() == Type.Kind.PARAMETERIZED_TYPE)
                    .map(Type::asParameterizedType)
                    .map(type -> type.arguments().get(0)) // ExceptionMapper<?> has a single type argument
                    .map(Type::name)
                    .findFirst()
                    .orElse(null);

            if (exceptionDotName == null) {
                continue;
            }

            MethodInfo toResponseMethod = classInfo.method(JaxRsConstants.TO_RESPONSE_METHOD_NAME,
                    Type.create(exceptionDotName, Type.Kind.CLASS));

            if (ResponseReader.hasResponseCodeValue(toResponseMethod)) {
                exceptionHandlerMap.put(exceptionDotName, ResponseReader.getResponseAnnotation(toResponseMethod));
            }

        }

        return exceptionHandlerMap;
    }

    /**
     * Extracts all methods from the provided class and its ancestors that are known to the instance's index
     * 
     * @param resource
     * @return all methods from the provided class and its ancestors
     */
    private List<MethodInfo> getResourceMethods(final AnnotationScannerContext context, ClassInfo resource) {
        Type resourceType = Type.create(resource.name(), Type.Kind.CLASS);
        Map<ClassInfo, Type> chain = JandexUtil.inheritanceChain(context.getIndex(), resource, resourceType);
        List<MethodInfo> methods = new ArrayList<>();

        for (ClassInfo classInfo : chain.keySet()) {
            methods.addAll(classInfo.methods());

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
     * Scans a sub-resource locator method's return type as a resource class. The list of locator path parameters
     * will be expanded with any parameters that apply to the resource sub-locator method (both path and operation
     * parameters).
     * 
     * @param openApi current OAI result
     * @param locatorPathParameters the parent resource's list of path parameters, may be null
     * @param resourceClass the JAX-RS resource class being processed. May be a sub-class of the class which declares method
     * @param method sub-resource locator JAX-RS method
     */
    private void processSubResource(final AnnotationScannerContext context,
            final ClassInfo resourceClass,
            final MethodInfo method,
            OpenAPI openApi,
            List<Parameter> locatorPathParameters) {
        final Type methodReturnType = method.returnType();

        if (Type.Kind.VOID.equals(methodReturnType.kind())) {
            // Can sub-resource locators return a CompletionStage?
            return;
        }

        ClassInfo subResourceClass = context.getIndex().getClassByName(methodReturnType.name());

        if (subResourceClass != null) {
            final String originalAppPath = this.currentAppPath;

            Function<AnnotationInstance, Parameter> reader = (t) -> {
                return ParameterReader.readParameter(context, t);
            };

            ParameterProcessor.ResourceParameters params = ParameterProcessor.process(context.getIndex(), resourceClass, method,
                    reader, context.getExtensions());

            this.currentAppPath = PathMaker.makePath(this.currentAppPath, params.getOperationPath());

            /*
             * Combine parameters passed previously with all of those from the current resource class and
             * method that apply to this Path. The full list will be used as PATH-LEVEL parameters for
             * sub-resource methods deeper in the scan.
             */
            processResourceClass(context, openApi, subResourceClass,
                    ListUtil.mergeNullableLists(locatorPathParameters,
                            params.getPathItemParameters(),
                            params.getOperationParameters()));

            this.currentAppPath = originalAppPath;
        }
    }

    /**
     * Process a single JAX-RS method to produce an OpenAPI Operation.
     * 
     * @param openApi
     * @param resourceClass
     * @param method
     * @param methodType
     * @param resourceTags
     * @param locatorPathParameters
     */
    private void processResourceMethod(final AnnotationScannerContext context,
            final ClassInfo resourceClass,
            final MethodInfo method,
            final PathItem.HttpMethod methodType,
            OpenAPI openApi,
            Set<String> resourceTags,
            List<Parameter> locatorPathParameters,
            Map<DotName, AnnotationInstance> exceptionAnnotationMap) {

        LOG.debugf("Processing jax-rs method: {0}", method.toString());

        // Figure out the current @Produces and @Consumes (if any)
        String[] currentConsumes = getMediaTypes(method, JaxRsConstants.CONSUMES);
        String[] currentProduces = getMediaTypes(method, JaxRsConstants.PRODUCES);
        CurrentScannerInfo.register(this, currentConsumes, currentProduces);

        // Process any @Operation annotation
        Optional<Operation> maybeOperation = processOperation(context, method);
        if (!maybeOperation.isPresent()) {
            return; // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
        }
        final Operation operation = maybeOperation.get();

        // Process tags - @Tag and @Tags annotations combines with the resource tags we've already found (passed in)
        Set<String> tags = processTags(method, openApi, true);
        if (tags == null) {
            if (!resourceTags.isEmpty()) {
                operation.setTags(new ArrayList<>(resourceTags));
            }
        } else if (!tags.isEmpty()) {
            operation.setTags(new ArrayList<>(tags));
        }

        // Process @Parameter annotations. TODO: this still mix Jax-rs and OpenAPI
        Function<AnnotationInstance, Parameter> reader = (t) -> {
            return ParameterReader.readParameter(context, t);
        };
        ParameterProcessor.ResourceParameters params = ParameterProcessor.process(context.getIndex(), resourceClass, method,
                reader, context.getExtensions());
        operation.setParameters(params.getOperationParameters());

        PathItem pathItem = new PathItemImpl();
        pathItem.setParameters(ListUtil.mergeNullableLists(locatorPathParameters, params.getPathItemParameters()));

        // Process any @RequestBody annotation (note: the @RequestBody annotation can be found on a method argument *or* on the method)
        RequestBody requestBody = processRequestBody(context, method, params);
        if (requestBody != null) {
            operation.setRequestBody(requestBody);
        }

        // Process @APIResponse annotations
        processResponse(context, method, operation, exceptionAnnotationMap);

        // Process @SecurityRequirement annotations
        processSecurityReuirementAnnotation(resourceClass, method, operation);

        // Process @Callback annotations
        /////////////////////////////////////////
        processCallback(context, method, operation);

        // Process @Server annotations
        processServerAnnotation(method, operation);

        // Process @Extension annotations
        processExtensions(context, method, operation);

        // Process Security Roles
        JavaSecurityProcessor.processSecurityRoles(method, operation);

        // Now set the operation on the PathItem as appropriate based on the Http method type
        ///////////////////////////////////////////
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

        // Figure out the path for the operation.  This is a combination of the App, Resource, and Method @Path annotations
        String path = PathMaker.makePath(this.currentAppPath, params.getOperationPath());

        // Get or create a PathItem to hold the operation
        PathItem existingPath = ModelUtil.paths(openApi).getPathItem(path);

        if (existingPath == null) {
            ModelUtil.paths(openApi).addPathItem(path, pathItem);
        } else {
            // Changes applied to 'existingPath', no need to re-assign or add to OAI.
            MergeUtil.mergeObjects(existingPath, pathItem);
        }
    }

    private void processResponse(final AnnotationScannerContext context, final MethodInfo method, Operation operation,
            Map<DotName, AnnotationInstance> exceptionAnnotationMap) {

        List<AnnotationInstance> apiResponseAnnotations = ResponseReader.getResponseAnnotations(method);
        for (AnnotationInstance annotation : apiResponseAnnotations) {
            addApiReponseFromAnnotation(context, annotation, operation);
        }
        /*
         * If there is no response from annotations, try to create one from the method return value.
         * Do not generate a response if the app has used an empty @ApiResponses annotation. This
         * provides a way for the application to indicate that responses will be supplied some other
         * way (i.e. static file).
         */
        AnnotationInstance apiResponses = ResponseReader.getResponsesAnnotation(method);
        if (apiResponses == null || !JandexUtil.isEmpty(apiResponses)) {
            createResponseFromJaxRsMethod(context, method, operation);
        }

        //Add api response using list of exceptions in the methods and exception mappers
        List<Type> methodExceptions = method.exceptions();

        for (Type type : methodExceptions) {
            DotName exceptionDotName = type.name();
            if (exceptionAnnotationMap.keySet().contains(exceptionDotName)) {
                AnnotationInstance exMapperApiResponseAnnotation = exceptionAnnotationMap.get(exceptionDotName);
                if (!this.responseCodeExistInMethodAnnotations(exMapperApiResponseAnnotation, apiResponseAnnotations)) {
                    addApiReponseFromAnnotation(context, exMapperApiResponseAnnotation, operation);
                }
            }
        }
    }

    /**
     * Add api response to api responses using the annotation information
     *
     * @param apiResponseAnnotation The api response annotation
     * @param operation the method operation
     */
    private void addApiReponseFromAnnotation(final AnnotationScannerContext context, AnnotationInstance apiResponseAnnotation,
            Operation operation) {
        String responseCode = ResponseReader.getResponseName(apiResponseAnnotation);
        if (responseCode == null) {
            responseCode = APIResponses.DEFAULT;
        }
        APIResponse response = ResponseReader.readResponse(context, apiResponseAnnotation);
        APIResponses responses = ModelUtil.responses(operation);
        responses.addAPIResponse(responseCode, response);
    }

    /**
     * Process the request body
     * 
     * @param context the current scanning context
     * @param method the resource method
     * @param params the params
     * @return RequestBody model
     */
    private RequestBody processRequestBody(final AnnotationScannerContext context, final MethodInfo method,
            ParameterProcessor.ResourceParameters params) {
        RequestBody requestBody = null;

        List<AnnotationInstance> requestBodyAnnotations = RequestBodyReader.getRequestBodyAnnotations(method);
        for (AnnotationInstance annotation : requestBodyAnnotations) {
            requestBody = RequestBodyReader.readRequestBody(context, annotation);
            Content formBodyContent = params.getFormBodyContent();

            if (formBodyContent != null) {
                // If form parameters were present, overlay RequestBody onto the generated form content
                requestBody.setContent((Content) MergeUtil.mergeObjects(formBodyContent, requestBody.getContent()));
            }

            // TODO if the method argument type is Request, don't generate a Schema!

            Type requestBodyType = null;
            if (annotation.target().kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
                requestBodyType = JandexUtil.getMethodParameterType(method,
                        annotation.target().asMethodParameter().position());
            } else if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
                requestBodyType = getRequestBodyParameterClassType(method, context.getExtensions(), this);
            }

            // Only generate the request body schema if the @RequestBody is not a reference and no schema is yet specified
            if (requestBodyType != null && requestBody.getRef() == null) {
                if (!ModelUtil.requestBodyHasSchema(requestBody)) {
                    Schema schema = SchemaFactory.typeToSchema(context.getIndex(), requestBodyType, context.getExtensions());

                    if (schema != null) {
                        ModelUtil.setRequestBodySchema(requestBody, schema, CurrentScannerInfo.getCurrentConsumes());
                    }
                }

                if (requestBody.getRequired() == null && TypeUtil.isOptional(requestBodyType)) {
                    requestBody.setRequired(Boolean.FALSE);
                }
            }
        }

        // If the request body is null, figure it out from the parameters.  Only if the
        // method declares that it @Consumes data
        if ((requestBody == null || (requestBody.getContent() == null && requestBody.getRef() == null))
                && CurrentScannerInfo.getCurrentConsumes() != null) {
            if (params.getFormBodySchema() != null) {
                if (requestBody == null) {
                    requestBody = new RequestBodyImpl();
                }
                Schema schema = params.getFormBodySchema();
                ModelUtil.setRequestBodySchema(requestBody, schema, CurrentScannerInfo.getCurrentConsumes());
            } else {
                Type requestBodyType = getRequestBodyParameterClassType(method, context.getExtensions(), this);

                if (requestBodyType != null) {
                    Schema schema = null;

                    if (RestEasyConstants.MULTIPART_INPUTS.contains(requestBodyType.name())) {
                        schema = new SchemaImpl();
                        schema.setType(Schema.SchemaType.OBJECT);
                    } else {
                        schema = SchemaFactory.typeToSchema(context.getIndex(), requestBodyType, context.getExtensions());
                    }

                    if (requestBody == null) {
                        requestBody = new RequestBodyImpl();
                    }

                    if (schema != null) {
                        ModelUtil.setRequestBodySchema(requestBody, schema, CurrentScannerInfo.getCurrentConsumes());
                    }

                    if (requestBody.getRequired() == null && TypeUtil.isOptional(requestBodyType)) {
                        requestBody.setRequired(Boolean.FALSE);
                    }
                }
            }
        }
        return requestBody;
    }

    /**
     * While scanning JAX-RS method, find the operations
     * 
     * @param context the scanning context
     * @param method the JAX-RS method
     * @return Maybe an Operation model
     */
    private Optional<Operation> processOperation(final AnnotationScannerContext context, final MethodInfo method) {
        if (OperationReader.methodHasOperationAnnotation(method)) {
            if (OperationReader.operationIsHidden(method)) {
                return Optional.empty();
            }
            AnnotationInstance operationAnnotation = OperationReader.getOperationAnnotation(method);
            return Optional.of(OperationReader.readOperation(context, operationAnnotation));
        } else {
            return Optional.of(new OperationImpl());
        }
    }

    /**
     * Check if the response code declared in the ExceptionMapper already defined in one of the ApiReponse annotations of the
     * method.
     * If the response code already exists then ignore the exception mapper annotation.
     *
     * @param exMapperApiResponseAnnotation ApiResponse annotation declared in the exception mapper
     * @param methodApiResponseAnnotations List of ApiResponse annotations declared in the jax-rs method.
     * @return response code exist or not
     */
    private boolean responseCodeExistInMethodAnnotations(AnnotationInstance exMapperApiResponseAnnotation,
            List<AnnotationInstance> methodApiResponseAnnotations) {
        AnnotationValue exMapperResponseCode = exMapperApiResponseAnnotation
                .value(ResponseConstant.PROP_RESPONSE_CODE);
        Optional<AnnotationInstance> apiResponseWithSameCode = methodApiResponseAnnotations.stream()
                .filter(annotationInstance -> {
                    AnnotationValue methodAnnotationValue = annotationInstance
                            .value(ResponseConstant.PROP_RESPONSE_CODE);
                    return (methodAnnotationValue != null && methodAnnotationValue.equals(exMapperResponseCode));
                }).findFirst();

        return apiResponseWithSameCode.isPresent();
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
    private Set<String> processTags(final AnnotationTarget target, OpenAPI openApi, final boolean nullWhenMissing) {
        if (!TagReader.hasTagAnnotation(target)) {
            return nullWhenMissing ? null : Collections.emptySet();
        }

        Set<String> tags = new LinkedHashSet<>();
        List<AnnotationInstance> tagAnnos = TagReader.getTagAnnotations(target);

        for (AnnotationInstance ta : tagAnnos) {
            if (JandexUtil.isRef(ta)) {
                tags.add(JandexUtil.value(ta, OpenApiConstants.REF));
            } else {
                Tag tag = TagReader.readTag(ta);

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

    static String[] getMediaTypes(MethodInfo resourceMethod, DotName annotationName) {
        AnnotationInstance annotation = resourceMethod.annotation(annotationName);

        if (annotation == null) {
            annotation = JandexUtil.getClassAnnotation(resourceMethod.declaringClass(), annotationName);
        }

        if (annotation != null) {
            AnnotationValue annotationValue = annotation.value();

            if (annotationValue != null) {
                return annotationValue.asStringArray();
            }

            return OpenApiConstants.DEFAULT_MEDIA_TYPES.get();
        }

        return null;
    }

    /**
     * Called when a jax-rs method's APIResponse annotations have all been processed but
     * no response was actually created for the operation. This method will create a response
     * from the method information and add it to the given operation. It will try to do this
     * by examining the method's return value and the type of operation (GET, PUT, POST, DELETE).
     *
     * If there is a return value of some kind (a non-void return type) then the response code
     * is assumed to be 200.
     *
     * If there not a return value (void return type) then either a 201 or 204 is returned,
     * depending on the type of request.
     *
     * TODO generate responses for each checked exception?
     * 
     * @param method
     * @param operation
     */
    private void createResponseFromJaxRsMethod(final AnnotationScannerContext context,
            final MethodInfo method,
            Operation operation) {
        Type returnType = method.returnType();
        APIResponse response = null;
        String code = "200";
        String description = "OK";

        if (returnType.kind() == Type.Kind.VOID) {
            boolean asyncResponse = method.parameters()
                    .stream()
                    .map(Type::name)
                    .anyMatch(JaxRsConstants.ASYNC_RESPONSE::equals);

            if (method.hasAnnotation(JaxRsConstants.POST)) {
                code = "201";
                description = "Created";
            } else if (!asyncResponse) {
                code = "204";
                description = "No Content";
            }

            if (generateResponse(code, operation)) {
                response = new APIResponseImpl().description(description);
            }
        } else if (generateResponse(code, operation)) {
            response = new APIResponseImpl().description(description);

            /*
             * Only generate content if not already supplied in annotations and the
             * method does not return an opaque JAX-RS Response
             */
            if (!returnType.name().equals(JaxRsConstants.RESPONSE) &&
                    (ModelUtil.responses(operation).getAPIResponse(code) == null ||
                            ModelUtil.responses(operation).getAPIResponse(code).getContent() == null)) {

                Schema schema;

                if (RestEasyConstants.MULTIPART_OUTPUTS.contains(returnType.name())) {
                    schema = new SchemaImpl();
                    schema.setType(Schema.SchemaType.OBJECT);
                } else {
                    schema = SchemaFactory.typeToSchema(context.getIndex(), returnType, context.getExtensions());
                }

                ContentImpl content = new ContentImpl();
                String[] produces = CurrentScannerInfo.getCurrentProduces();

                if (produces == null || produces.length == 0) {
                    produces = OpenApiConstants.DEFAULT_MEDIA_TYPES.get();
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
                responses.removeAPIResponse(code);

                // Overlay the information from the annotations (2nd arg) onto the generated details (1st)
                response = MergeUtil.mergeObjects(response, responseFromAnnotations);
            }

            responses.addAPIResponse(code, response);
        }
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
    private boolean generateResponse(String status, Operation operation) {
        APIResponses responses = operation.getResponses();
        return responses == null || responses.getAPIResponse(status) != null;
    }

    private void setCurrentAppPath(String path) {
        this.currentAppPath = path;
    }

    /**
     * Process a certain class for OpenApiDefinition annotations.
     * TODO: Could move a level up maybe ?
     * 
     * @param context the scanning context
     * @param targetClass the class that contain the server annotation
     * @param openApi the current OpenApi model being created
     */
    private void processDefinitionAnnotation(final AnnotationScannerContext context, final ClassInfo targetClass,
            OpenAPI openApi) {
        AnnotationInstance openApiDefAnno = DefinitionReader.getDefinitionAnnotation(targetClass);
        if (openApiDefAnno != null) {
            DefinitionReader.processDefinition(context, openApi, openApiDefAnno);
        }
    }

    /**
     * Process a certain class for server annotations.
     * TODO: Could move a level up maybe ?
     * 
     * @param targetClass the class that contain the server annotation
     * @param openApi the current OpenApi model being created
     */
    private void processServerAnnotation(final ClassInfo targetClass, OpenAPI openApi) {
        List<AnnotationInstance> serverAnnotations = ServerReader.getServerAnnotations(targetClass);
        for (AnnotationInstance annotation : serverAnnotations) {
            Server server = ServerReader.readServer(annotation);
            openApi.addServer(server);
        }
    }

    /**
     * Process a certain method for server annotations.
     * TODO: Could move a level up maybe ?
     * 
     * @param method the method that contain the server annotation
     * @param operation the current Operation model being created
     */
    private void processServerAnnotation(final MethodInfo method, Operation operation) {
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
     * Process a certain class for security annotations.
     * TODO: Could move a level up maybe ?
     * 
     * @param targetClass the class that contain the security annotation
     * @param openApi the current OpenApi model being created
     */
    private void processSecuritySchemeAnnotation(final ClassInfo targetClass, OpenAPI openApi) {
        List<AnnotationInstance> securitySchemeAnnotations = SecuritySchemeReader.getSecuritySchemeAnnotations(targetClass);

        for (AnnotationInstance annotation : securitySchemeAnnotations) {
            String name = SecuritySchemeReader.getSecuritySchemeName(annotation);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                SecurityScheme securityScheme = SecuritySchemeReader.readSecurityScheme(annotation);
                Components components = ModelUtil.components(openApi);
                components.addSecurityScheme(name, securityScheme);
            }
        }
    }

    /**
     * Get the security requirements on method and class and add them to the openapi model
     * 
     * @param resourceClass the class
     * @param method the method
     * @param operation the operation to add them to
     */
    private void processSecurityReuirementAnnotation(final ClassInfo resourceClass, final MethodInfo method,
            Operation operation) {

        List<AnnotationInstance> securityRequirementAnnotations = SecurityRequirementReader
                .getSecurityRequirementAnnotations(method);
        securityRequirementAnnotations.addAll(SecurityRequirementReader.getSecurityRequirementAnnotations(resourceClass));

        for (AnnotationInstance annotation : securityRequirementAnnotations) {
            SecurityRequirement requirement = SecurityRequirementReader.readSecurityRequirement(annotation);
            if (requirement != null) {
                operation.addSecurityRequirement(requirement);
            }
        }
    }

    /**
     * Process a callback annotation
     * 
     * @param context the scanning context
     * @param method the method
     * @param operation the operation to add this to
     */
    private void processCallback(final AnnotationScannerContext context, final MethodInfo method, Operation operation) {
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

    private void processExtensions(final AnnotationScannerContext context, final MethodInfo method, Operation operation) {
        List<AnnotationInstance> extensionAnnotations = ExtensionReader.getExtensionsAnnotations(method);

        if (extensionAnnotations.isEmpty()) {
            extensionAnnotations.addAll(ExtensionReader.getExtensionsAnnotations(method.declaringClass()));
        }
        for (AnnotationInstance annotation : extensionAnnotations) {
            String name = ExtensionReader.getExtensionName(annotation);
            operation.addExtension(name, ExtensionReader.readExtensionValue(context, name, annotation));
        }
    }

    private void sortTags(OpenAPI openApi, boolean tagsDefined) {
        // Sort the tags unless the application has defined the order in OpenAPIDefinition annotation(s)
        if (!tagsDefined && openApi.getTags() != null) {
            openApi.setTags(openApi.getTags()
                    .stream()
                    .sorted(Comparator.comparing(Tag::getName))
                    .collect(Collectors.toList()));
        }
    }

    private void sortPaths(OpenAPI openApi) {
        // Now that all paths have been created, sort them (we don't have a better way to organize them).
        Paths paths = openApi.getPaths();
        if (paths != null) {
            Paths sortedPaths = new PathsImpl();
            TreeSet<String> sortedKeys = new TreeSet<>(paths.getPathItems().keySet());
            for (String pathKey : sortedKeys) {
                PathItem pathItem = paths.getPathItem(pathKey);
                sortedPaths.addPathItem(pathKey, pathItem);
            }
            sortedPaths.setExtensions(paths.getExtensions());
            openApi.setPaths(sortedPaths);
        }
    }

    /**
     * Use the Jandex index to find all jax-rs resource classes. This is done by searching for
     * all Class-level @Path annotations.
     * 
     * @param index IndexView
     * @return Collection of ClassInfo's
     */
    private Collection<ClassInfo> getJaxRsResourceClasses(IndexView index) {
        return index.getAnnotations(JaxRsConstants.PATH)
                .stream()
                .map(AnnotationInstance::target)
                .filter(target -> target.kind() == AnnotationTarget.Kind.CLASS)
                .map(AnnotationTarget::asClass)
                .filter(classInfo -> !Modifier.isInterface(classInfo.flags()) ||
                        index.getAllKnownImplementors(classInfo.name()).stream()
                                .anyMatch(info -> !Modifier.isAbstract(info.flags())))
                .distinct() // CompositeIndex instances may return duplicates
                .collect(Collectors.toList());
    }

    /**
     * Go through the method parameters looking for one that is not annotated with a jax-rs
     * annotation.That will be the one that is the request body.
     * 
     * @param method MethodInfo
     * @param extensions available extensions
     * @param annotationScanner the current scanner
     * @return Type
     */
    private static Type getRequestBodyParameterClassType(MethodInfo method, List<AnnotationScannerExtension> extensions,
            AnnotationScanner annotationScanner) {
        List<Type> methodParams = method.parameters();
        if (methodParams.isEmpty()) {
            return null;
        }
        for (short i = 0; i < methodParams.size(); i++) {
            List<AnnotationInstance> parameterAnnotations = JandexUtil.getParameterAnnotations(method, i);
            if (parameterAnnotations.isEmpty()
                    || !containsScannerAnnotations(parameterAnnotations, extensions)) {
                return methodParams.get(i);
            }
        }
        return null;
    }

    private static boolean containsScannerAnnotations(List<AnnotationInstance> instances,
            List<AnnotationScannerExtension> extensions) {
        for (AnnotationInstance instance : instances) {
            if (ParameterProcessor.JaxRsParameter.isParameter(instance.name())) {
                return true;
            }
            if (instance.name().toString().startsWith(JAXRS_PACKAGE)) {
                return true;
            }
            for (AnnotationScannerExtension extension : extensions) {
                if (extension.isScannerAnnotationExtension(instance))
                    return true;
            }
        }
        return false;
    }

}
