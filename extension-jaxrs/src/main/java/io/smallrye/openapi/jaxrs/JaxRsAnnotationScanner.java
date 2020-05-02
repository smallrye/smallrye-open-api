package io.smallrye.openapi.jaxrs;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.api.util.ListUtil;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.CurrentScannerInfo;
import io.smallrye.openapi.runtime.io.parameter.ParameterReader;
import io.smallrye.openapi.runtime.io.response.ResponseReader;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.PathMaker;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.processor.JavaSecurityProcessor;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * Scanner that scan Jax-Rs entry points.
 * This is also the default, as it's part of the spec.
 * 
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class JaxRsAnnotationScanner implements AnnotationScanner {
    private static final String JAXRS_PACKAGE = "javax.ws.rs";
    private String currentAppPath = "";

    @Override
    public String getName() {
        return "JAX-RS";
    }

    @Override
    public boolean isAsyncResponse(final MethodInfo method) {
        return method.parameters()
                .stream()
                .map(Type::name)
                .anyMatch(JaxRsConstants.ASYNC_RESPONSE::equals);
    }

    @Override
    public boolean isPostMethod(final MethodInfo method) {
        return method.hasAnnotation(JaxRsConstants.POST);
    }

    @Override
    public boolean isScannerInternalResponse(Type returnType) {
        return returnType.name().equals(JaxRsConstants.RESPONSE);
    }

    @Override
    public boolean isMultipartOutput(Type returnType) {
        return RestEasyConstants.MULTIPART_OUTPUTS.contains(returnType.name());
    }

    @Override
    public boolean isMultipartInput(Type inputType) {
        return RestEasyConstants.MULTIPART_INPUTS.contains(inputType.name());
    }

    @Override
    public String getReasonPhrase(int statusCode) {
        Status status = Status.fromStatusCode(statusCode);
        return status != null ? status.getReasonPhrase() : null;
    }

    @Override
    public boolean containsScannerAnnotations(List<AnnotationInstance> instances,
            List<AnnotationScannerExtension> extensions) {
        for (AnnotationInstance instance : instances) {
            if (JaxRsParameter.isParameter(instance.name())) {
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

    @Override
    public OpenAPI scan(final AnnotationScannerContext context, OpenAPI openApi) {
        // Get all JaxRs applications and convert them to OpenAPI models (and merge them into a single one)
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

    /**
     * Find and process all JAX-RS applications
     * 
     * @param context the scanning context
     * @param openApi the openAPI model
     */
    private void processApplicationClasses(final AnnotationScannerContext context, OpenAPI openApi) {
        // Get all JaxRs applications and convert them to OpenAPI models (and merge them into a single one)
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
        AnnotationInstance applicationPathAnnotation = JandexUtil.getClassAnnotation(applicationClass,
                JaxRsConstants.APPLICATION_PATH);
        if (applicationPathAnnotation == null || context.getConfig().applicationPathDisable()) {
            applicationPathAnnotation = JandexUtil.getClassAnnotation(applicationClass, JaxRsConstants.PATH);
        }
        // TODO: Add support for Application selection when there are more than one
        if (applicationPathAnnotation != null) {
            this.currentAppPath = applicationPathAnnotation.value().asString();
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
        JaxRsLogging.log.processingClass(resourceClass.simpleName());

        // Process @SecurityScheme annotations.
        processSecuritySchemeAnnotation(resourceClass, openApi);

        // Process Java security
        processJavaSecurity(resourceClass, openApi);

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

        // Process tags (both declarations and references).
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

            Function<AnnotationInstance, Parameter> reader = t -> ParameterReader.readParameter(context, t);

            ResourceParameters params = ParameterProcessor.process(context.getIndex(), resourceClass, method,
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

        JaxRsLogging.log.processingMethod(method.toString());

        // Figure out the current @Produces and @Consumes (if any)
        CurrentScannerInfo.setCurrentConsumes(getMediaTypes(method, JaxRsConstants.CONSUMES).orElse(null));
        CurrentScannerInfo.setCurrentProduces(getMediaTypes(method, JaxRsConstants.PRODUCES).orElse(null));

        // Process any @Operation annotation
        Optional<Operation> maybeOperation = processOperation(context, method);
        if (!maybeOperation.isPresent()) {
            return; // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
        }
        final Operation operation = maybeOperation.get();

        // Process tags - @Tag and @Tags annotations combines with the resource tags we've already found (passed in)
        processOperationTags(method, openApi, resourceTags, operation);

        // Process @Parameter annotations.
        Function<AnnotationInstance, Parameter> reader = t -> ParameterReader.readParameter(context, t);

        ResourceParameters params = ParameterProcessor.process(context.getIndex(), resourceClass, method,
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
        processSecurityRequirementAnnotation(resourceClass, method, operation);

        // Process @Callback annotations
        processCallback(context, method, operation);

        // Process @Server annotations
        processServerAnnotation(method, operation);

        // Process @Extension annotations
        processExtensions(context, method, operation);

        // Process Security Roles
        JavaSecurityProcessor.processSecurityRoles(method, operation);

        // Now set the operation on the PathItem as appropriate based on the Http method type
        setOperationOnPathItem(methodType, pathItem, operation);

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

    //    /**
    //     * Process the request body
    //     * 
    //     * @param context the current scanning context
    //     * @param method the resource method
    //     * @param params the params
    //     * @return RequestBody model
    //     */
    //    private RequestBody processRequestBody(final AnnotationScannerContext context, final MethodInfo method,
    //            ResourceParameters params) {
    //        RequestBody requestBody = null;
    //
    //        List<AnnotationInstance> requestBodyAnnotations = RequestBodyReader.getRequestBodyAnnotations(method);
    //        for (AnnotationInstance annotation : requestBodyAnnotations) {
    //            requestBody = RequestBodyReader.readRequestBody(context, annotation);
    //            Content formBodyContent = params.getFormBodyContent();
    //
    //            if (formBodyContent != null) {
    //                // If form parameters were present, overlay RequestBody onto the generated form content
    //                requestBody.setContent(MergeUtil.mergeObjects(formBodyContent, requestBody.getContent()));
    //            }
    //
    //            // TODO if the method argument type is Request, don't generate a Schema!
    //
    //            Type requestBodyType = null;
    //            if (annotation.target().kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
    //                requestBodyType = JandexUtil.getMethodParameterType(method,
    //                        annotation.target().asMethodParameter().position());
    //            } else if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
    //                requestBodyType = getRequestBodyParameterClassType(method, context.getExtensions());
    //            }
    //
    //            // Only generate the request body schema if the @RequestBody is not a reference and no schema is yet specified
    //            if (requestBodyType != null && requestBody.getRef() == null) {
    //                if (!ModelUtil.requestBodyHasSchema(requestBody)) {
    //                    Schema schema = SchemaFactory.typeToSchema(context.getIndex(), requestBodyType, context.getExtensions());
    //
    //                    if (schema != null) {
    //                        ModelUtil.setRequestBodySchema(requestBody, schema, CurrentScannerInfo.getCurrentConsumes());
    //                    }
    //                }
    //
    //                if (requestBody.getRequired() == null && TypeUtil.isOptional(requestBodyType)) {
    //                    requestBody.setRequired(Boolean.FALSE);
    //                }
    //            }
    //        }
    //
    //        // If the request body is null, figure it out from the parameters.  Only if the
    //        // method declares that it @Consumes data
    //        if ((requestBody == null || (requestBody.getContent() == null && requestBody.getRef() == null))
    //                && CurrentScannerInfo.getCurrentConsumes() != null) {
    //            if (params.getFormBodySchema() != null) {
    //                if (requestBody == null) {
    //                    requestBody = new RequestBodyImpl();
    //                }
    //                Schema schema = params.getFormBodySchema();
    //                ModelUtil.setRequestBodySchema(requestBody, schema, CurrentScannerInfo.getCurrentConsumes());
    //            } else {
    //                Type requestBodyType = getRequestBodyParameterClassType(method, context.getExtensions());
    //
    //                if (requestBodyType != null) {
    //                    Schema schema = null;
    //
    //                    if (RestEasyConstants.MULTIPART_INPUTS.contains(requestBodyType.name())) {
    //                        schema = new SchemaImpl();
    //                        schema.setType(Schema.SchemaType.OBJECT);
    //                    } else {
    //                        schema = SchemaFactory.typeToSchema(context.getIndex(), requestBodyType, context.getExtensions());
    //                    }
    //
    //                    if (requestBody == null) {
    //                        requestBody = new RequestBodyImpl();
    //                    }
    //
    //                    if (schema != null) {
    //                        ModelUtil.setRequestBodySchema(requestBody, schema, CurrentScannerInfo.getCurrentConsumes());
    //                    }
    //
    //                    if (requestBody.getRequired() == null && TypeUtil.isOptional(requestBodyType)) {
    //                        requestBody.setRequired(Boolean.FALSE);
    //                    }
    //                }
    //            }
    //        }
    //        return requestBody;
    //    }

    static Optional<String[]> getMediaTypes(MethodInfo resourceMethod, DotName annotationName) {
        AnnotationInstance annotation = resourceMethod.annotation(annotationName);

        if (annotation == null) {
            annotation = JandexUtil.getClassAnnotation(resourceMethod.declaringClass(), annotationName);
        }

        if (annotation != null) {
            AnnotationValue annotationValue = annotation.value();

            if (annotationValue != null) {
                return Optional.of(annotationValue.asStringArray());
            }

            return Optional.of(OpenApiConstants.DEFAULT_MEDIA_TYPES.get());
        }

        return Optional.empty();
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

    //    /**
    //     * Go through the method parameters looking for one that is not annotated with a jax-rs
    //     * annotation.That will be the one that is the request body.
    //     * 
    //     * @param method MethodInfo
    //     * @param extensions available extensions
    //     * @return Type
    //     */
    //    private static Type getRequestBodyParameterClassType(MethodInfo method, List<AnnotationScannerExtension> extensions) {
    //        List<Type> methodParams = method.parameters();
    //        if (methodParams.isEmpty()) {
    //            return null;
    //        }
    //        for (short i = 0; i < methodParams.size(); i++) {
    //            List<AnnotationInstance> parameterAnnotations = JandexUtil.getParameterAnnotations(method, i);
    //            if (parameterAnnotations.isEmpty()
    //                    || !containsScannerAnnotations(parameterAnnotations, extensions)) {
    //                return methodParams.get(i);
    //            }
    //        }
    //        return null;
    //    }

}
