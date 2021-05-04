package io.smallrye.openapi.jaxrs;

import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import io.smallrye.openapi.runtime.io.response.ResponseReader;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.scanner.processor.JavaSecurityProcessor;
import io.smallrye.openapi.runtime.scanner.spi.AbstractAnnotationScanner;
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
public class JaxRsAnnotationScanner extends AbstractAnnotationScanner {
    private static final String JAVAX_PACKAGE = "javax.ws.rs";
    private static final String JAKARTA_PACKAGE = "jakarta.ws.rs";

    private final Deque<JaxRsSubResourceLocator> subResourceStack = new LinkedList<>();

    @Override
    public String getName() {
        return "JAX-RS";
    }

    @Override
    public boolean isAsyncResponse(final MethodInfo method) {
        return method.parameters()
                .stream()
                .map(Type::name)
                .anyMatch(JaxRsConstants.ASYNC_RESPONSE::contains);
    }

    @Override
    public boolean isPostMethod(final MethodInfo method) {
        return JandexUtil.hasAnyOneOfAnnotation(method, JaxRsConstants.POST);
    }

    @Override
    public boolean isDeleteMethod(final MethodInfo method) {
        return JandexUtil.hasAnyOneOfAnnotation(method, JaxRsConstants.DELETE);
    }

    @Override
    public boolean isScannerInternalResponse(Type returnType) {
        return JaxRsConstants.RESPONSE.contains(returnType.name());
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
    public boolean containsScannerAnnotations(List<AnnotationInstance> instances,
            List<AnnotationScannerExtension> extensions) {

        if (containsJavaxAnnotations(instances) || containsJakartaAnnotations(instances)) {
            return true;
        }

        for (AnnotationInstance instance : instances) {
            for (AnnotationScannerExtension extension : extensions) {
                if (extension.isScannerAnnotationExtension(instance))
                    return true;
            }
        }
        return false;
    }

    static boolean containsJavaxAnnotations(List<AnnotationInstance> instances) {
        return containsJaxrsAnnotations(instances, JAVAX_PACKAGE);
    }

    static boolean containsJakartaAnnotations(List<AnnotationInstance> instances) {
        return containsJaxrsAnnotations(instances, JAKARTA_PACKAGE);
    }

    private static boolean containsJaxrsAnnotations(List<AnnotationInstance> instances, String packageIndicator) {
        for (AnnotationInstance instance : instances) {
            if (JaxRsParameter.isParameter(instance.name())) {
                return true;
            }
            if (instance.name().toString().startsWith(packageIndicator)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public OpenAPI scan(final AnnotationScannerContext context, OpenAPI openApi) {
        // Get all JaxRs applications and convert them to OpenAPI models (and merge them into a single one)
        processApplicationClasses(context, openApi);

        // Now find all jax-rs endpoints
        processResourceClasses(context, openApi);

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
        Collection<ClassInfo> applications = new ArrayList<>();
        for (DotName applicationindicator : JaxRsConstants.APPLICATION) {
            applications.addAll(context.getIndex().getAllKnownSubclasses(applicationindicator));
        }
        // this can be a useful extension point to set/override the application path
        processScannerExtensions(context, applications);

        for (ClassInfo classInfo : applications) {
            OpenAPI applicationOpenApi = processApplicationClass(context, classInfo);
            openApi = MergeUtil.merge(openApi, applicationOpenApi);
        }
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
        processSecuritySchemeAnnotation(context, applicationClass, openApi);

        // Process @Server annotations
        processServerAnnotation(applicationClass, openApi);

        return openApi;
    }

    private void processResourceClasses(final AnnotationScannerContext context, OpenAPI openApi) {
        // Now find all jax-rs endpoints
        Collection<ClassInfo> resourceClasses = getJaxRsResourceClasses(context);
        for (ClassInfo resourceClass : resourceClasses) {
            TypeResolver resolver = TypeResolver.forClass(context, resourceClass, null);
            context.getResolverStack().push(resolver);
            processResourceClass(context, openApi, resourceClass, null);
            context.getResolverStack().pop();
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
        processSecuritySchemeAnnotation(context, resourceClass, openApi);

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
        Set<String> tagRefs = processTags(context, resourceClass, openApi, false);

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
                        processResourceMethod(context, resourceClass, methodInfo, httpMethod, tagRefs,
                                locatorPathParameters, exceptionAnnotationMap);
                    });

            if (resourceCount.get() == 0 && JandexUtil.hasAnyOneOfAnnotation(methodInfo, JaxRsConstants.PATH)) {
                processSubResource(context, resourceClass, methodInfo, openApi, locatorPathParameters);
            }
        }
    }

    /**
     * Build a map between exception class name and its corresponding @ApiResponse annotation in the jax-rs exception mapper
     * 
     */
    private Map<DotName, AnnotationInstance> processExceptionMappers(final AnnotationScannerContext context) {
        Collection<ClassInfo> exceptionMappers = new ArrayList<>();

        for (DotName dn : JaxRsConstants.EXCEPTION_MAPPER) {
            exceptionMappers.addAll(context.getIndex()
                    .getKnownDirectImplementors(dn));
        }

        return exceptionMappers.stream()
                .flatMap(this::exceptionResponseAnnotations)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Stream<Entry<DotName, AnnotationInstance>> exceptionResponseAnnotations(ClassInfo classInfo) {
        return classInfo.interfaceTypes()
                .stream()
                .filter(it -> JaxRsConstants.EXCEPTION_MAPPER.contains(it.name()))
                .filter(it -> Type.Kind.PARAMETERIZED_TYPE.equals(it.kind()))
                .map(Type::asParameterizedType)
                .map(type -> type.arguments().get(0)) // ExceptionMapper<?> has a single type argument
                .map(type -> entryOf(type.name(), classInfo.method(JaxRsConstants.TO_RESPONSE_METHOD_NAME, type)))
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .filter(entry -> ResponseReader.hasResponseCodeValue(entry.getValue()))
                .map(entry -> entryOf(entry.getKey(), ResponseReader.getResponseAnnotation(entry.getValue())));
    }

    // Replace with Map.entry when available (Java 9+)
    static <K, V> Entry<K, V> entryOf(K key, V value) {
        return new SimpleEntry<>(key, value);
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
        final Type methodReturnType = context.getResourceTypeResolver().resolve(method.returnType());

        if (Type.Kind.VOID.equals(methodReturnType.kind())) {
            // Can sub-resource locators return a CompletionStage?
            return;
        }

        JaxRsSubResourceLocator locator = new JaxRsSubResourceLocator(resourceClass, method);
        ClassInfo subResourceClass = context.getIndex().getClassByName(methodReturnType.name());

        // Do not allow the same resource locator method to be used twice (sign of infinite recursion)
        if (subResourceClass != null && !this.subResourceStack.contains(locator)) {
            Function<AnnotationInstance, Parameter> reader = t -> ParameterReader.readParameter(context, t);

            ResourceParameters params = JaxRsParameterProcessor.process(context, resourceClass, method,
                    reader, context.getExtensions());

            final String originalAppPath = this.currentAppPath;
            final String subResourcePath;

            if (this.subResourceStack.isEmpty()) {
                subResourcePath = params.getFullOperationPath();
            } else {
                // If we are already processing a sub-resource, ignore any @Path information from the current class
                subResourcePath = params.getOperationPath();
            }

            this.currentAppPath = createPathFromSegments(this.currentAppPath, subResourcePath);
            this.subResourceStack.push(locator);

            TypeResolver resolver = TypeResolver.forClass(context, subResourceClass, methodReturnType);
            context.getResolverStack().push(resolver);

            /*
             * Combine parameters passed previously with all of those from the current resource class and
             * method that apply to this Path. The full list will be used as PATH-LEVEL parameters for
             * sub-resource methods deeper in the scan.
             */
            processResourceClass(context, openApi, subResourceClass,
                    ListUtil.mergeNullableLists(locatorPathParameters,
                            params.getPathItemParameters(),
                            params.getOperationParameters()));

            context.getResolverStack().pop();
            this.subResourceStack.pop();
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
            Set<String> resourceTags,
            List<Parameter> locatorPathParameters,
            Map<DotName, AnnotationInstance> exceptionAnnotationMap) {

        JaxRsLogging.log.processingMethod(method.toString());

        // Figure out the current @Produces and @Consumes (if any)
        CurrentScannerInfo.setCurrentConsumes(getMediaTypes(method, JaxRsConstants.CONSUMES,
                context.getConfig().getDefaultConsumes().orElse(OpenApiConstants.DEFAULT_MEDIA_TYPES.get()))
                        .orElse(null));
        CurrentScannerInfo.setCurrentProduces(getMediaTypes(method, JaxRsConstants.PRODUCES,
                context.getConfig().getDefaultProduces().orElse(OpenApiConstants.DEFAULT_MEDIA_TYPES.get()))
                        .orElse(null));

        // Process any @Operation annotation
        Optional<Operation> maybeOperation = processOperation(context, method);
        if (!maybeOperation.isPresent()) {
            return; // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
        }
        final Operation operation = maybeOperation.get();

        // Process tags - @Tag and @Tags annotations combines with the resource tags we've already found (passed in)
        processOperationTags(context, method, context.getOpenApi(), resourceTags, operation);

        // Process @Parameter annotations.
        Function<AnnotationInstance, Parameter> reader = t -> ParameterReader.readParameter(context, t);

        ResourceParameters params = JaxRsParameterProcessor.process(context, resourceClass, method,
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
        final String path;

        if (this.subResourceStack.isEmpty()) {
            path = super.makePath(params.getFullOperationPath());
        } else {
            // When processing a sub-resource tree, ignore any @Path information from the current class
            path = super.makePath(params.getOperationPath());
        }

        // Get or create a PathItem to hold the operation
        PathItem existingPath = ModelUtil.paths(context.getOpenApi()).getPathItem(path);

        if (existingPath == null) {
            ModelUtil.paths(context.getOpenApi()).addPathItem(path, pathItem);
        } else {
            // Changes applied to 'existingPath', no need to re-assign or add to OAI.
            MergeUtil.mergeObjects(existingPath, pathItem);
        }
    }

    static Optional<String[]> getMediaTypes(MethodInfo resourceMethod, Set<DotName> annotationName, String[] defaultValue) {
        AnnotationInstance annotation = JandexUtil.getAnnotation(resourceMethod, annotationName);

        if (annotation == null) {
            annotation = JandexUtil.getClassAnnotation(resourceMethod.declaringClass(), annotationName);
        }

        if (annotation != null) {
            AnnotationValue annotationValue = annotation.value();

            if (annotationValue != null) {
                return Optional.of(annotationValue.asStringArray());
            }

            return Optional.of(defaultValue);
        }

        return Optional.empty();
    }

    /**
     * Use the Jandex index to find all jax-rs resource classes. This is done by searching for
     * all Class-level @Path annotations.
     * 
     * @param context current scanning context
     * @return Collection of ClassInfo's
     */
    private Collection<ClassInfo> getJaxRsResourceClasses(AnnotationScannerContext context) {
        Collection<AnnotationInstance> pathAnnotations = new ArrayList<>();

        for (DotName dn : JaxRsConstants.PATH) {
            pathAnnotations.addAll(context.getIndex()
                    .getAnnotations(dn));
        }

        return pathAnnotations
                .stream()
                .map(AnnotationInstance::target)
                .filter(target -> target.kind() == AnnotationTarget.Kind.CLASS)
                .map(AnnotationTarget::asClass)
                .filter(classInfo -> this.hasImplementationOrIsIncluded(context, classInfo))
                .distinct() // CompositeIndex instances may return duplicates
                .collect(Collectors.toList());
    }

    private boolean hasImplementationOrIsIncluded(AnnotationScannerContext context, ClassInfo clazz) {
        if (!Modifier.isInterface(clazz.flags())) {
            return true;
        }

        FilteredIndexView filteredIndex = context.getIndex();

        if (filteredIndex.getAllKnownImplementors(clazz.name()).stream().anyMatch(this::notAbstract)) {
            return true;
        }

        return filteredIndex.explicitlyAccepts(clazz.name());
    }

    private boolean notAbstract(ClassInfo clazz) {
        return !Modifier.isAbstract(clazz.flags());
    }
}
