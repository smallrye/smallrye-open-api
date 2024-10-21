package io.smallrye.openapi.vertx;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.microprofile.openapi.OASFactory;
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

import io.smallrye.openapi.api.util.ListUtil;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.scanner.spi.AbstractAnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.ModelUtil;

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
    public boolean isAsyncResponse(final MethodInfo method) {
        // TODO: Implement this.
        return false;
    }

    @Override
    public boolean isPostMethod(final MethodInfo method) {
        return hasRouteMethod(method, "POST");
    }

    @Override
    public boolean isDeleteMethod(final MethodInfo method) {
        return hasRouteMethod(method, "DELETE");
    }

    @Override
    public boolean containsScannerAnnotations(List<AnnotationInstance> instances,
            List<AnnotationScannerExtension> extensions) {
        for (AnnotationInstance instance : instances) {
            if (VertxParameter.isParameter(instance.name())) {
                return true;
            }
            if (instance.name().toString().startsWith(VERTX_PACKAGE)
                    && !instance.name().equals(VertxConstants.REQUEST_BODY)) {
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
    public boolean isScannerInternalParameter(Type parameterType) {
        return VertxConstants.INTERNAL_PARAMETERS.contains(parameterType.name());
    }

    @Override
    public OpenAPI scan(final AnnotationScannerContext context, OpenAPI openApi) {
        this.context = context;
        // Get all Vert.x routes and convert them to OpenAPI models (and merge them into a single one)
        processRoutes(openApi);

        return openApi;
    }

    private boolean hasRouteMethod(final MethodInfo method, final String httpMethod) {
        if (method.hasAnnotation(VertxConstants.ROUTE)) {
            AnnotationInstance annotation = method.annotation(VertxConstants.ROUTE);
            AnnotationValue value = annotation.value("methods");
            return value != null && value.asEnumArray().length > 0
                    && Arrays.asList(value.asEnumArray()).contains(httpMethod);
        }
        return false;
    }

    /**
     * Find and process all Vert.x Routes
     *
     * @param context the scanning context
     * @param openApi the openAPI model
     */
    private void processRoutes(OpenAPI openApi) {
        // Get all Vert.x routes and convert them to OpenAPI models (and merge them into a single one)
        Collection<AnnotationInstance> routeAnnotations = context.getIndex()
                .getAnnotations(VertxConstants.ROUTE);
        Set<ClassInfo> applications = new LinkedHashSet<>();
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
            OpenAPI applicationOpenApi = processRouteClass(controller);
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
    private OpenAPI processRouteClass(ClassInfo routeClass) {

        VertxLogging.log.processingRouteClass(routeClass.simpleName());

        TypeResolver resolver = TypeResolver.forClass(context, routeClass, null);
        context.getResolverStack().push(resolver);

        // Get the @RouteBase info and save it for later
        AnnotationInstance routeBaseAnnotation = context.annotations().getAnnotation(routeClass,
                VertxConstants.ROUTE_BASE);

        if (routeBaseAnnotation != null) {
            this.currentAppPath = routeBaseAnnotation.value("path").asString(); // TODO: Check if there and check for :
        } else {
            this.currentAppPath = "/";
        }

        // Process @OpenAPIDefinition annotation
        OpenAPI openApi = processDefinitionAnnotation(context, routeClass);

        // Process @SecurityScheme annotations
        processSecuritySchemeAnnotation(context, routeClass, openApi);

        // Process @Server annotations
        processServerAnnotation(context, routeClass, openApi);

        // Process Java security
        processJavaSecurity(context, routeClass, openApi);

        // Now find and process the operation methods
        processRouteMethods(routeClass, openApi, null);

        context.getResolverStack().pop();

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
    private void processRouteMethods(final ClassInfo resourceClass,
            OpenAPI openApi,
            List<Parameter> locatorPathParameters) {

        // Process tags (both declarations and references).
        Set<String> tagRefs = processTags(context, resourceClass, openApi, false);

        getResourceMethods(context, resourceClass)
                .stream()
                .filter(m -> m.hasAnnotation(VertxConstants.ROUTE))
                .filter(this::shouldScan)
                .forEach(methodInfo -> Optional
                        .ofNullable(context.annotations().<String[]> getAnnotationValue(methodInfo, VertxConstants.ROUTE,
                                "methods"))
                        .map(methods -> Arrays.stream(methods).map(PathItem.HttpMethod::valueOf))
                        .orElseGet(() -> Arrays.stream(PathItem.HttpMethod.values()))
                        .forEach(httpMethod -> processRouteMethod(resourceClass, methodInfo, httpMethod, openApi,
                                tagRefs,
                                locatorPathParameters)));
    }

    @Override
    public String[] getDefaultConsumes(AnnotationScannerContext context, MethodInfo methodInfo, ResourceParameters params) {
        return context.getConfig().getDefaultConsumes().orElseGet(ContentIO::defaultMediaTypes);
    }

    @Override
    public String[] getDefaultProduces(AnnotationScannerContext context, MethodInfo methodInfo) {
        return context.getConfig().getDefaultProduces().orElseGet(ContentIO::defaultMediaTypes);
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
    private void processRouteMethod(final ClassInfo resourceClass,
            final MethodInfo method,
            final PathItem.HttpMethod methodType,
            OpenAPI openApi,
            Set<String> resourceTags,
            List<Parameter> locatorPathParameters) {

        VertxLogging.log.processingMethod(method.toString());

        // Figure out the current @Produces and @Consumes (if any)
        String[] defaultConsumes = getDefaultConsumes(context, method, getResourceParameters(resourceClass, method));
        context.setDefaultConsumes(defaultConsumes);
        context.setCurrentConsumes(getMediaTypes(method, VertxConstants.ROUTE_CONSUMES,
                defaultConsumes).orElse(null));
        String[] defaultProduces = getDefaultProduces(context, method);
        context.setDefaultProduces(defaultProduces);
        context.setCurrentProduces(getMediaTypes(method, VertxConstants.ROUTE_PRODUCES,
                defaultProduces).orElse(null));

        // Process any @Operation annotation
        Optional<Operation> maybeOperation = processOperation(context, resourceClass, method);
        if (!maybeOperation.isPresent()) {
            return; // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
        }
        final Operation operation = maybeOperation.get();

        // Process tags - @Tag and @Tags annotations combines with the resource tags we've already found (passed in)
        processOperationTags(context, method, openApi, resourceTags, operation);

        // Process @Parameter annotations.
        PathItem pathItem = OASFactory.createPathItem();
        ResourceParameters params = getResourceParameters(resourceClass, method);
        operation.setParameters(params.getOperationParameters());

        pathItem.setParameters(ListUtil.mergeNullableLists(locatorPathParameters, params.getPathItemParameters()));

        // Process any @RequestBody annotation (note: the @RequestBody annotation can be found on a method argument *or* on the method)
        RequestBody requestBody = processRequestBody(context, method, params);
        if (requestBody != null) {
            operation.setRequestBody(requestBody);
        }

        // Process @APIResponse annotations
        processResponse(context, resourceClass, method, operation, null);

        // Process @SecurityRequirement annotations
        processSecurityRequirementAnnotation(context, resourceClass, method, operation);

        // Process @Callback annotations
        processCallback(context, method, operation);

        // Process @Server annotations
        processServerAnnotation(context, method, operation);

        // Process @Extension annotations
        processExtensions(context, method, operation);

        // Process Security Roles
        context.getJavaSecurityProcessor().processSecurityRoles(method, operation);

        // Now set the operation on the PathItem as appropriate based on the Http method type
        pathItem.setOperation(methodType, operation);

        if (!processProfiles(context.getConfig(), operation)) {
            return;
        }

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

    private ResourceParameters getResourceParameters(final ClassInfo resourceClass,
            final MethodInfo method) {
        Function<AnnotationInstance, Parameter> reader = t -> context.io().parameterIO().read(t);
        return VertxParameterProcessor.process(context, currentAppPath, resourceClass,
                method, reader,
                context.getExtensions());
    }

    /**
     * Determine if the route method should be scanned or skipped.
     *
     * Skip when:
     * <ol>
     * <li>The route's {@code type} is {@code FAILURE}
     * <li>The route specifies a {@code regex} pattern, unless also annotated with
     * {@link org.eclipse.microprofile.openapi.annotations.Operation @Operation}.
     * </ol>
     *
     * @param resourceMethod a resource method annotated with {@code @Route}
     * @return true if the method should be scanned, otherwise false
     */
    boolean shouldScan(MethodInfo resourceMethod) {
        AnnotationInstance route = resourceMethod.annotation(VertxConstants.ROUTE);

        if ("FAILURE".equals(context.annotations().value(route, "type"))) {
            return false;
        }

        if (context.annotations().value(route, "regex") != null) {
            return Objects.nonNull(context.io().operationIO().getAnnotation(resourceMethod));
        }

        return true;
    }

    Optional<String[]> getMediaTypes(MethodInfo resourceMethod, String property, String[] defaultValue) {
        DotName annotationName = VertxConstants.ROUTE;

        AnnotationInstance annotation = resourceMethod.annotation(annotationName);

        if (annotation == null || annotation.value(property) == null) {
            annotation = context.annotations().getAnnotation(resourceMethod.declaringClass(), VertxConstants.ROUTE_BASE);
        }

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
