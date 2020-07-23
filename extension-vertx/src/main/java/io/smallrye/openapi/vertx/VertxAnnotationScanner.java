package io.smallrye.openapi.vertx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import io.smallrye.openapi.runtime.scanner.processor.JavaSecurityProcessor;
import io.smallrye.openapi.runtime.scanner.spi.AbstractAnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.vertx.core.http.HttpMethod;

/**
 * Scanner that scan Vertx routes.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class VertxAnnotationScanner extends AbstractAnnotationScanner {
    private static final String VERTX_PACKAGE = "io.vertx.ext.web";

    @Override
    public String getName() {
        return "Vert.x";
    }

    @Override
    public boolean isWrapperType(Type type) {
        return type.kind().equals(Type.Kind.PARAMETERIZED_TYPE);
    }

    @Override
    public boolean isAsyncResponse(final MethodInfo method) {
        // TODO: Implement this.
        return false;
    }

    @Override
    public boolean isPostMethod(final MethodInfo method) {
        if (hasRouteMethod(method, HttpMethod.POST)) {
            return true;
        }
        return false;

    }

    @Override
    public boolean isDeleteMethod(final MethodInfo method) {
        if (hasRouteMethod(method, HttpMethod.DELETE)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isScannerInternalResponse(Type returnType) {
        // If it's Response Entity that does not have a valid type, then stop
        //        return returnType.name().equals(SpringConstants.RESPONSE_ENTITY)
        //                && !returnType.kind().equals(Type.Kind.PARAMETERIZED_TYPE);
        return false;
    }

    @Override
    public boolean isMultipartOutput(Type returnType) {
        // TODO: Implement this.
        return false; //SpringConstants.MULTIPART_OUTPUTS.contains(returnType.name());
    }

    @Override
    public boolean isMultipartInput(Type inputType) {
        // TODO: Implement this.
        return false; //SpringConstants.MULTIPART_INPUTS.contains(inputType.name());
    }

    @Override
    public String getReasonPhrase(int statusCode) {
        // TODO: Implement this
        //HttpStatus status = HttpStatus.resolve(statusCode);
        //return status != null ? status.getReasonPhrase() : null;
        return "OK";
    }

    @Override
    public boolean containsScannerAnnotations(List<AnnotationInstance> instances,
            List<AnnotationScannerExtension> extensions) {
        for (AnnotationInstance instance : instances) {
            if (VertxParameter.isParameter(instance.name())) {
                return true;
            }
            if (instance.name().toString().startsWith(VERTX_PACKAGE)) {
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
        // Get all Vert.x routes and convert them to OpenAPI models (and merge them into a single one)
        processRoutes(context, openApi);

        boolean tagsDefined = openApi.getTags() != null && !openApi.getTags().isEmpty();

        // Sort the tags unless the application has defined the order in OpenAPIDefinition annotation(s)
        sortTags(openApi, tagsDefined);

        // Now that all paths have been created, sort them (we don't have a better way to organize them).
        sortPaths(openApi);

        return openApi;
    }

    private boolean hasRouteMethod(final MethodInfo method, final HttpMethod httpMethod) {
        if (method.hasAnnotation(VertxConstants.ROUTE)) {
            AnnotationInstance annotation = method.annotation(VertxConstants.ROUTE);
            AnnotationValue value = annotation.value("methods");
            return value != null && value.asEnumArray().length > 0
                    && Arrays.asList(value.asEnumArray()).contains(httpMethod.name());
        }
        return false;
    }

    /**
     * Find and process all Vert.x Routes
     *
     * @param context the scanning context
     * @param openApi the openAPI model
     */
    private void processRoutes(final AnnotationScannerContext context, OpenAPI openApi) {
        // Get all Vert.x routes and convert them to OpenAPI models (and merge them into a single one)
        Collection<AnnotationInstance> routeAnnotations = context.getIndex()
                .getAnnotations(VertxConstants.ROUTE);
        List<ClassInfo> applications = new ArrayList<>();
        for (AnnotationInstance annotationInstance : routeAnnotations) {
            if (annotationInstance.target().kind().equals(AnnotationTarget.Kind.METHOD)) {
                ClassInfo classInfo = annotationInstance.target().asMethod().declaringClass();
                applications.add(classInfo);
            } else {
                VertxLogging.log.ignoringAnnotation(VertxConstants.ROUTE.withoutPackagePrefix());
            }
        }

        // this can be a useful extension point to set/override the application path
        processScannerExtensions(context, applications);

        for (ClassInfo controller : applications) {
            OpenAPI applicationOpenApi = processRouteClass(context, controller);
            openApi = MergeUtil.merge(openApi, applicationOpenApi);
        }
    }

    /**
     * Processes a Class that contains routes and creates an {@link OpenAPI} model. Performs
     * annotation scanning and other processing. Returns a model unique to that single Class.
     *
     * @param context the scanning context
     * @param routeClass the class containing the Vert.x route
     */
    private OpenAPI processRouteClass(final AnnotationScannerContext context, ClassInfo routeClass) {

        VertxLogging.log.processingRouteClass(routeClass.simpleName());

        OpenAPI openApi = new OpenAPIImpl();
        openApi.setOpenapi(OpenApiConstants.OPEN_API_VERSION);

        // Get the @RouteBase info and save it for later
        AnnotationInstance routeBaseAnnotation = JandexUtil.getClassAnnotation(routeClass,
                VertxConstants.ROUTE_BASE);

        if (routeBaseAnnotation != null) {
            this.currentAppPath = routeBaseAnnotation.value("path").asString(); // TODO: Check if there and check for :
        } else {
            this.currentAppPath = "/";
        }

        // Process @OpenAPIDefinition annotation
        processDefinitionAnnotation(context, routeClass, openApi);

        // Process @SecurityScheme annotations
        processSecuritySchemeAnnotation(routeClass, openApi);

        // Process @Server annotations
        processServerAnnotation(routeClass, openApi);

        // Process Java security
        processJavaSecurity(routeClass, openApi);

        // Now find and process the operation methods
        processRouteMethods(context, routeClass, openApi, null);

        return openApi;
    }

    /**
     * Process the Vert.x Route Operation methods
     *
     * @param context the scanning context
     * @param resourceClass the class containing the methods
     * @param openApi the OpenApi model being processed
     * @param locatorPathParameters path parameters
     */
    private void processRouteMethods(final AnnotationScannerContext context,
            final ClassInfo resourceClass,
            OpenAPI openApi,
            List<Parameter> locatorPathParameters) {

        // Process tags (both declarations and references).
        Set<String> tagRefs = processTags(resourceClass, openApi, false);

        for (MethodInfo methodInfo : getResourceMethods(context, resourceClass)) {
            if (methodInfo.annotations().size() > 0) {
                // Try @Route annotations
                if (methodInfo.hasAnnotation(VertxConstants.ROUTE)) {
                    AnnotationInstance requestMappingAnnotation = methodInfo.annotation(VertxConstants.ROUTE);
                    AnnotationValue methodValue = requestMappingAnnotation.value("methods");
                    if (methodValue != null) {
                        String[] enumArray = methodValue.asEnumArray();
                        for (String enumValue : enumArray) {
                            if (enumValue != null) {
                                PathItem.HttpMethod httpMethod = PathItem.HttpMethod.valueOf(enumValue.toUpperCase());
                                processRouteMethod(context, resourceClass, methodInfo, httpMethod, openApi, tagRefs,
                                        locatorPathParameters);
                            }
                        }
                    } else {
                        // TODO: Default ? Look at RouteBase
                    }
                }

            }
        }
    }

    /**
     * Process a single Vert.x method to produce an OpenAPI Operation.
     *
     * @param openApi
     * @param resourceClass
     * @param method
     * @param methodType
     * @param resourceTags
     * @param locatorPathParameters
     */
    private void processRouteMethod(final AnnotationScannerContext context,
            final ClassInfo resourceClass,
            final MethodInfo method,
            final PathItem.HttpMethod methodType,
            OpenAPI openApi,
            Set<String> resourceTags,
            List<Parameter> locatorPathParameters) {

        VertxLogging.log.processingMethod(method.toString());

        // Figure out the current @Produces and @Consumes (if any)
        CurrentScannerInfo.setCurrentConsumes(getMediaTypes(method, MediaTypeProperty.consumes).orElse(null));
        CurrentScannerInfo.setCurrentProduces(getMediaTypes(method, MediaTypeProperty.produces).orElse(null));

        // Process any @Operation annotation
        Optional<Operation> maybeOperation = processOperation(context, method);
        if (!maybeOperation.isPresent()) {
            return; // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
        }
        final Operation operation = maybeOperation.get();

        // Process tags - @Tag and @Tags annotations combines with the resource tags we've already found (passed in)
        processOperationTags(method, openApi, resourceTags, operation);

        // Process @Parameter annotations.
        PathItem pathItem = new PathItemImpl();
        Function<AnnotationInstance, Parameter> reader = t -> ParameterReader.readParameter(context, t);

        ResourceParameters params = ParameterProcessor.process(context.getIndex(), resourceClass, method, reader,
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

    static Optional<String[]> getMediaTypes(MethodInfo resourceMethod, MediaTypeProperty property) {
        DotName annotationName = VertxConstants.ROUTE;

        AnnotationInstance annotation = resourceMethod.annotation(annotationName);

        if (annotation == null || annotation.value(property.name()) == null) {
            annotation = JandexUtil.getClassAnnotation(resourceMethod.declaringClass(), VertxConstants.ROUTE_BASE);
        }

        if (annotation != null) {
            AnnotationValue annotationValue = annotation.value(property.name());

            if (annotationValue != null) {
                return Optional.of(annotationValue.asStringArray());
            }

            return Optional.of(OpenApiConstants.DEFAULT_MEDIA_TYPES.get());
        }

        return Optional.empty();
    }

    enum MediaTypeProperty {
        consumes,
        produces
    }
}
