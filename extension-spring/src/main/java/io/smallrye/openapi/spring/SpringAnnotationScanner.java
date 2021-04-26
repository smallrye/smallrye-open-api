package io.smallrye.openapi.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.api.util.ListUtil;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.CurrentScannerInfo;
import io.smallrye.openapi.runtime.io.parameter.ParameterReader;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.scanner.processor.JavaSecurityProcessor;
import io.smallrye.openapi.runtime.scanner.spi.AbstractAnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * Scanner that scan Spring entry points.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SpringAnnotationScanner extends AbstractAnnotationScanner {
    private static final String SPRING_PACKAGE = "org.springframework.web";

    @Override
    public String getName() {
        return "Spring";
    }

    @Override
    public boolean isWrapperType(Type type) {
        return type.name().equals(SpringConstants.RESPONSE_ENTITY) && type.kind().equals(Type.Kind.PARAMETERIZED_TYPE);
    }

    @Override
    public boolean isAsyncResponse(final MethodInfo method) {
        // TODO: Implement this.
        return false;
    }

    @Override
    public boolean isPostMethod(final MethodInfo method) {
        if (hasRequestMappingMethod(method, "POST")) {
            return true;
        }
        return method.hasAnnotation(SpringConstants.POST_MAPPING);

    }

    @Override
    public boolean isDeleteMethod(final MethodInfo method) {
        if (hasRequestMappingMethod(method, "DELETE")) {
            return true;
        }
        return method.hasAnnotation(SpringConstants.DELETE_MAPPING);
    }

    @Override
    public boolean isScannerInternalResponse(Type returnType) {
        // If it's Response Entity that does not have a valid type, then stop
        return returnType.name().equals(SpringConstants.RESPONSE_ENTITY)
                && !returnType.kind().equals(Type.Kind.PARAMETERIZED_TYPE);
    }

    @Override
    public boolean isMultipartOutput(Type returnType) {
        // TODO: Check this
        return SpringConstants.MULTIPART_OUTPUTS.contains(returnType.name());
    }

    @Override
    public boolean isMultipartInput(Type inputType) {
        // TODO: Check this
        return SpringConstants.MULTIPART_INPUTS.contains(inputType.name());
    }

    @Override
    public boolean containsScannerAnnotations(List<AnnotationInstance> instances,
            List<AnnotationScannerExtension> extensions) {
        for (AnnotationInstance instance : instances) {
            if (SpringParameter.isParameter(instance.name())) {
                return true;
            }
            if (instance.name().toString().startsWith(SPRING_PACKAGE)
                    && !instance.name().equals(SpringConstants.REQUEST_BODY)) {
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
        // Get all Spring controllers and convert them to OpenAPI models (and merge them into a single one)
        processControllerClasses(context, openApi);

        return openApi;
    }

    private boolean hasRequestMappingMethod(final MethodInfo method, final String requestMethod) {
        if (method.hasAnnotation(SpringConstants.REQUEST_MAPPING)) {
            AnnotationInstance annotation = method.annotation(SpringConstants.REQUEST_MAPPING);
            AnnotationValue value = annotation.value("method");
            return value != null && value.asEnumArray().length > 0
                    && Arrays.asList(value.asEnumArray()).contains(requestMethod);
        }
        return false;
    }

    /**
     * Find and process all Spring Controllers
     * TODO: Also support org.springframework.stereotype.Controller annotations ?
     *
     * @param context the scanning context
     * @param openApi the openAPI model
     */
    private void processControllerClasses(final AnnotationScannerContext context, OpenAPI openApi) {
        // Get all Spring controllers and convert them to OpenAPI models (and merge them into a single one)
        Collection<AnnotationInstance> controllerAnnotations = context.getIndex()
                .getAnnotations(SpringConstants.REST_CONTROLLER);
        List<ClassInfo> applications = new ArrayList<>();
        for (AnnotationInstance annotationInstance : controllerAnnotations) {
            if (annotationInstance.target().kind().equals(AnnotationTarget.Kind.CLASS)) {
                ClassInfo classInfo = annotationInstance.target().asClass();
                applications.add(classInfo);
            } else {
                SpringLogging.log.ignoringAnnotation(SpringConstants.REST_CONTROLLER.withoutPackagePrefix());
            }
        }

        // this can be a useful extension point to set/override the application path
        processScannerExtensions(context, applications);

        for (ClassInfo controller : applications) {
            OpenAPI applicationOpenApi = processControllerClass(context, controller);
            openApi = MergeUtil.merge(openApi, applicationOpenApi);
        }
    }

    /**
     * Processes a Spring Controller and creates an {@link OpenAPI} model. Performs
     * annotation scanning and other processing. Returns a model unique to that single Spring
     * controller.
     *
     * @param context the scanning context
     * @param controllerClass the Spring REST controller
     */
    private OpenAPI processControllerClass(final AnnotationScannerContext context, ClassInfo controllerClass) {

        SpringLogging.log.processingController(controllerClass.simpleName());

        TypeResolver resolver = TypeResolver.forClass(context, controllerClass, null);
        context.getResolverStack().push(resolver);

        OpenAPI openApi = new OpenAPIImpl();
        openApi.setOpenapi(OpenApiConstants.OPEN_API_VERSION);

        // Get the @RequestMapping info and save it for later
        AnnotationInstance requestMappingAnnotation = JandexUtil.getClassAnnotation(controllerClass,
                SpringConstants.REQUEST_MAPPING);

        if (requestMappingAnnotation != null) {
            this.currentAppPath = SpringParameterProcessor.requestMappingValuesToPath(requestMappingAnnotation);
        } else {
            this.currentAppPath = "/";
        }

        // Process @OpenAPIDefinition annotation
        processDefinitionAnnotation(context, controllerClass, openApi);

        // Process @SecurityScheme annotations
        processSecuritySchemeAnnotation(context, controllerClass, openApi);

        // Process @Server annotations
        processServerAnnotation(controllerClass, openApi);

        // Process Java security
        processJavaSecurity(controllerClass, openApi);

        // Now find and process the operation methods
        processControllerMethods(context, controllerClass, openApi, null);

        context.getResolverStack().pop();

        return openApi;
    }

    /**
     * Process the Spring controller Operation methods
     *
     * @param context the scanning context
     * @param resourceClass the class containing the methods
     * @param openApi the OpenApi model being processed
     * @param locatorPathParameters path parameters
     */
    private void processControllerMethods(final AnnotationScannerContext context,
            final ClassInfo resourceClass,
            OpenAPI openApi,
            List<Parameter> locatorPathParameters) {

        // Process tags (both declarations and references).
        Set<String> tagRefs = processTags(context, resourceClass, openApi, false);

        for (MethodInfo methodInfo : getResourceMethods(context, resourceClass)) {
            if (!methodInfo.annotations().isEmpty()) {
                // Try @XXXMapping annotations
                for (DotName validMethodAnnotations : SpringConstants.HTTP_METHODS) {
                    if (methodInfo.hasAnnotation(validMethodAnnotations)) {
                        String toHttpMethod = toHttpMethod(validMethodAnnotations);
                        PathItem.HttpMethod httpMethod = PathItem.HttpMethod.valueOf(toHttpMethod);
                        processControllerMethod(context, resourceClass, methodInfo, httpMethod, openApi, tagRefs,
                                locatorPathParameters);

                    }
                }

                // Try @RequestMapping
                if (methodInfo.hasAnnotation(SpringConstants.REQUEST_MAPPING)) {
                    AnnotationInstance requestMappingAnnotation = methodInfo.annotation(SpringConstants.REQUEST_MAPPING);
                    AnnotationValue methodValue = requestMappingAnnotation.value("method");
                    if (methodValue != null) {
                        String[] enumArray = methodValue.asEnumArray();
                        for (String enumValue : enumArray) {
                            if (enumValue != null) {
                                PathItem.HttpMethod httpMethod = PathItem.HttpMethod.valueOf(enumValue.toUpperCase());
                                processControllerMethod(context, resourceClass, methodInfo, httpMethod, openApi, tagRefs,
                                        locatorPathParameters);
                            }
                        }
                    } else {
                        // TODO: Default ?
                    }
                }

            }
        }
    }

    private String toHttpMethod(DotName dotname) {
        String className = dotname.withoutPackagePrefix();
        className = className.replace("Mapping", "");
        return className.toUpperCase();
    }

    /**
     * Process a single Spring method to produce an OpenAPI Operation.
     *
     * @param openApi
     * @param resourceClass
     * @param method
     * @param methodType
     * @param resourceTags
     * @param locatorPathParameters
     */
    private void processControllerMethod(final AnnotationScannerContext context,
            final ClassInfo resourceClass,
            final MethodInfo method,
            final PathItem.HttpMethod methodType,
            OpenAPI openApi,
            Set<String> resourceTags,
            List<Parameter> locatorPathParameters) {

        SpringLogging.log.processingMethod(method.toString());

        // Figure out the current @Produces and @Consumes (if any)
        CurrentScannerInfo.setCurrentConsumes(getMediaTypes(method, SpringConstants.MAPPING_CONSUMES,
                context.getConfig().getDefaultConsumes().orElse(OpenApiConstants.DEFAULT_MEDIA_TYPES.get())).orElse(null));

        CurrentScannerInfo.setCurrentProduces(getMediaTypes(method, SpringConstants.MAPPING_PRODUCES,
                context.getConfig().getDefaultProduces().orElse(OpenApiConstants.DEFAULT_MEDIA_TYPES.get())).orElse(null));

        // Process any @Operation annotation
        Optional<Operation> maybeOperation = processOperation(context, method);
        if (!maybeOperation.isPresent()) {
            return; // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
        }
        final Operation operation = maybeOperation.get();

        // Process tags - @Tag and @Tags annotations combines with the resource tags we've already found (passed in)
        processOperationTags(context, method, openApi, resourceTags, operation);

        // Process @Parameter annotations.
        PathItem pathItem = new PathItemImpl();
        Function<AnnotationInstance, Parameter> reader = t -> ParameterReader.readParameter(context, t);
        ResourceParameters params = SpringParameterProcessor.process(context, resourceClass,
                method, reader,
                context.getExtensions());
        operation.setParameters(params.getOperationParameters());

        pathItem.setParameters(ListUtil.mergeNullableLists(locatorPathParameters, params.getPathItemParameters()));

        // Process any @RequestBody annotation (note: the @RequestBody annotation can be found on a method argument *or* on the method)
        RequestBody requestBody = processRequestBody(context, method, params);
        if (requestBody != null) {
            operation.setRequestBody(requestBody);
        }

        // Process @APIResponse annotations
        processResponse(context, method, operation, null);

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
        String path = super.makePath(params.getOperationPath());

        // Get or create a PathItem to hold the operation
        PathItem existingPath = ModelUtil.paths(openApi).getPathItem(path);

        if (existingPath == null) {
            ModelUtil.paths(openApi).addPathItem(path, pathItem);
        } else {
            // Changes applied to 'existingPath', no need to re-assign or add to OAI.
            MergeUtil.mergeObjects(existingPath, pathItem);
        }
    }

    static Optional<String[]> getMediaTypes(MethodInfo resourceMethod, String property, String[] defaultValue) {
        Set<DotName> annotationNames = new HashSet<>(SpringConstants.HTTP_METHODS);
        annotationNames.add(SpringConstants.REQUEST_MAPPING);

        // Check methods
        for (DotName annotationName : annotationNames) {
            AnnotationInstance annotation = resourceMethod.annotation(annotationName);
            if (annotation != null) {
                AnnotationValue annotationValue = annotation.value(property);
                if (annotationValue != null) {
                    return Optional.of(annotationValue.asStringArray());
                }
            }
        }

        // Check class
        AnnotationInstance annotation = JandexUtil.getClassAnnotation(resourceMethod.declaringClass(),
                SpringConstants.REQUEST_MAPPING);
        if (annotation != null) {
            AnnotationValue annotationValue = annotation.value(property);
            if (annotationValue != null) {
                return Optional.of(annotationValue.asStringArray());
            }

            return Optional.of(defaultValue);
        }

        return Optional.empty();
    }
}
