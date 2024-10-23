package io.smallrye.openapi.jaxrs;

import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.util.ListUtil;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.scanner.spi.AbstractAnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
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

    private static final Predicate<DotName> isParameter = JaxRsParameter::isParameter;
    private static final Predicate<DotName> inJaxRsPackage = name -> {
        String nameString = name.toString();
        return nameString.startsWith(JAKARTA_PACKAGE) || nameString.startsWith(JAVAX_PACKAGE);
    };

    private final Deque<JaxRsSubResourceLocator> subResourceStack = new LinkedList<>();

    @Override
    public String getName() {
        return "JAX-RS";
    }

    @Override
    public boolean isWrapperType(Type type) {
        return type.name().equals(RestEasyConstants.REACTIVE_REST_RESPONSE)
                && type.kind().equals(Type.Kind.PARAMETERIZED_TYPE);
    }

    @Override
    public boolean isAsyncResponse(final MethodInfo method) {
        return method.parameterTypes()
                .stream()
                .map(Type::name)
                .anyMatch(JaxRsConstants.ASYNC_RESPONSE::contains);
    }

    @Override
    public boolean isPostMethod(final MethodInfo method) {
        return context.annotations().hasAnnotation(method, JaxRsConstants.POST);
    }

    @Override
    public boolean isDeleteMethod(final MethodInfo method) {
        return context.annotations().hasAnnotation(method, JaxRsConstants.DELETE);
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
    public boolean isFrameworkContextType(Type type) {
        return JaxRsConstants.CONTEXTS.contains(type.name());
    }

    @Override
    public boolean containsScannerAnnotations(List<AnnotationInstance> instances,
            List<AnnotationScannerExtension> extensions) {

        if (containsJaxrsAnnotations(instances)) {
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

    static boolean containsJaxrsAnnotations(List<AnnotationInstance> instances) {
        return instances.stream()
                .map(AnnotationInstance::name)
                .anyMatch(isParameter.or(inJaxRsPackage));
    }

    @Override
    public OpenAPI scan(final AnnotationScannerContext context, OpenAPI openApi) {
        this.context = context;

        // Get all JaxRs applications and convert them to OpenAPI models (and merge them into a single one)
        processApplicationClasses(openApi);

        // Now find all jax-rs endpoints
        processResourceClasses(openApi);

        return openApi;
    }

    /**
     * Find and process all JAX-RS applications
     *
     * @param context the scanning context
     * @param openApi the openAPI model
     */
    private void processApplicationClasses(OpenAPI openApi) {
        // Get all JaxRs applications and convert them to OpenAPI models (and merge them into a single one)
        Collection<ClassInfo> applications = new ArrayList<>();
        for (DotName applicationindicator : JaxRsConstants.APPLICATION) {
            applications.addAll(context.getIndex().getAllKnownSubclasses(applicationindicator));
        }
        // this can be a useful extension point to set/override the application path
        processScannerExtensions(context, applications);

        for (ClassInfo classInfo : applications) {
            OpenAPI applicationOpenApi = processApplicationClass(classInfo);
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
    private OpenAPI processApplicationClass(ClassInfo applicationClass) {
        // Get the @ApplicationPath info and save it for later (also support @Path which seems nonstandard but common).
        AnnotationInstance applicationPathAnnotation = context.annotations().getAnnotation(applicationClass,
                JaxRsConstants.APPLICATION_PATH);
        if (applicationPathAnnotation == null || context.getConfig().applicationPathDisable()) {
            applicationPathAnnotation = context.annotations().getAnnotation(applicationClass, JaxRsConstants.PATH);
        }
        // TODO: Add support for Application selection when there are more than one
        if (applicationPathAnnotation != null) {
            this.currentAppPath = applicationPathAnnotation.value().asString();
        } else {
            this.currentAppPath = "/";
        }

        // Process @OpenAPIDefinition annotation
        OpenAPI openApi = processDefinitionAnnotation(context, applicationClass);

        // Process @SecurityScheme annotations
        processSecuritySchemeAnnotation(context, applicationClass, openApi);

        // Process @Server annotations
        processServerAnnotation(context, applicationClass, openApi);

        return openApi;
    }

    private void processResourceClasses(OpenAPI openApi) {
        // Now find all jax-rs endpoints
        Collection<ClassInfo> resourceClasses = new ArrayList<>();
        resourceClasses.addAll(getJaxRsResourceClasses());
        resourceClasses.addAll(getConfigurationResourceClasses());

        for (ClassInfo resourceClass : resourceClasses) {
            TypeResolver resolver = TypeResolver.forClass(context, resourceClass, null);
            context.getResolverStack().push(resolver);
            // Process tags (both declarations and references).
            Set<String> tags = processResourceClassTags(openApi, resourceClass);
            processResourceClass(openApi, resourceClass, null, tags);
            context.getResolverStack().pop();
        }
    }

    /**
     * Find Tag annotations on the resource class or any super type. Stop searching the
     * class hierarchy when any Tags are found
     *
     * @param openApi
     * @param resourceClass
     * @return set of tag names found on the resourceClass or inherited from a super type
     */
    private Set<String> processResourceClassTags(OpenAPI openApi, ClassInfo resourceClass) {
        Set<String> tags = null;

        while ((tags = processTags(context, resourceClass, openApi, true)) == null) {
            Type superType = resourceClass.superClassType();
            resourceClass = superType != null ? context.getAugmentedIndex().getClass(superType) : null;

            if (resourceClass == null) {
                tags = Collections.emptySet();
                break;
            }
        }

        return tags;
    }

    /**
     * Processing a single JAX-RS resource class (annotated with @Path).
     *
     * @param openApi
     * @param resourceClass
     * @param locatorPathParameters
     */
    private void processResourceClass(OpenAPI openApi,
            ClassInfo resourceClass,
            List<Parameter> locatorPathParameters,
            Set<String> tagRefs) {
        JaxRsLogging.log.processingClass(resourceClass.simpleName());

        // Process @SecurityScheme annotations.
        processSecuritySchemeAnnotation(context, resourceClass, openApi);

        // Process Java security
        processJavaSecurity(context, resourceClass, openApi);

        // Now find and process the operation methods
        processResourceMethods(resourceClass, openApi, locatorPathParameters, tagRefs);
    }

    /**
     * Process the JAX-RS Operation methods
     *
     * @param context the scanning context
     * @param resourceClass the class containing the methods
     * @param openApi the OpenApi model being processed
     * @param locatorPathParameters path parameters
     */
    private void processResourceMethods(final ClassInfo resourceClass,
            OpenAPI openApi,
            List<Parameter> locatorPathParameters,
            Set<String> tagRefs) {

        // Process exception mapper to auto generate api response based on method exceptions
        Map<DotName, Map<String, APIResponse>> exceptionResponseMap = processExceptionMappers();
        List<MethodInfo> methods = getResourceMethods(context, resourceClass);
        Collections.reverse(methods);

        for (MethodInfo methodInfo : methods) {
            final AtomicInteger resourceCount = new AtomicInteger(0);

            JaxRsConstants.HTTP_METHODS
                    .stream()
                    .filter(methodInfo::hasAnnotation)
                    .map(DotName::withoutPackagePrefix)
                    .map(PathItem.HttpMethod::valueOf)
                    .distinct() // needed when both javax+jakarta annotations are present
                    .forEach(httpMethod -> {
                        resourceCount.incrementAndGet();
                        processResourceMethod(resourceClass, methodInfo, httpMethod, tagRefs,
                                locatorPathParameters, exceptionResponseMap);
                    });

            if (resourceCount.get() == 0 && context.annotations().hasAnnotation(methodInfo, JaxRsConstants.PATH)) {
                processSubResource(resourceClass, methodInfo, openApi, locatorPathParameters, tagRefs);
            }
        }
    }

    /**
     * Build a map between exception class name and its corresponding @ApiResponse annotation in the jax-rs exception mapper
     *
     */
    private Map<DotName, Map<String, APIResponse>> processExceptionMappers() {
        Collection<ClassInfo> exceptionMappers = new ArrayList<>();

        for (DotName dn : JaxRsConstants.EXCEPTION_MAPPER) {
            exceptionMappers.addAll(context.getIndex()
                    .getKnownDirectImplementors(dn));
        }

        return exceptionMappers.stream()
                .flatMap(this::exceptionResponseAnnotations)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Stream<Entry<DotName, Map<String, APIResponse>>> exceptionResponseAnnotations(ClassInfo classInfo) {

        Type exceptionType = classInfo.interfaceTypes()
                .stream()
                .filter(it -> JaxRsConstants.EXCEPTION_MAPPER.contains(it.name()))
                .filter(it -> Type.Kind.PARAMETERIZED_TYPE.equals(it.kind()))
                .map(Type::asParameterizedType)
                .map(type -> type.arguments().get(0))
                .findAny()
                .orElse(null);

        if (exceptionType == null) {
            return Stream.empty();
        }

        Stream<Entry<String, APIResponse>> methodAnnotations = Stream
                .of(classInfo.method(JaxRsConstants.TO_RESPONSE_METHOD_NAME, exceptionType))
                .filter(Objects::nonNull)
                .flatMap(m -> context.io()
                        .apiResponsesIO()
                        .readAll(m)
                        .entrySet()
                        .stream());

        Stream<Entry<String, APIResponse>> classAnnotations = context.io()
                .apiResponsesIO()
                .readAll(classInfo)
                .entrySet()
                .stream();

        // Later annotations will eventually override earlier ones, so put class before method
        BinaryOperator<APIResponse> latest = (v1, v2) -> v2;

        Map<String, APIResponse> annotations = Stream
                .concat(classAnnotations, methodAnnotations)
                //.filter(ResponseReader::hasResponseCodeValue)
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, latest, LinkedHashMap::new));

        if (annotations.isEmpty()) {
            return Stream.empty();
        } else {
            return Stream.of(entryOf(exceptionType.name(), annotations));
        }
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
    private void processSubResource(final ClassInfo resourceClass,
            final MethodInfo method,
            OpenAPI openApi,
            List<Parameter> locatorPathParameters,
            Set<String> tagRefs) {
        final Type methodReturnType = context.getResourceTypeResolver().resolve(method.returnType());

        if (Type.Kind.VOID.equals(methodReturnType.kind())) {
            // Can sub-resource locators return a CompletionStage?
            return;
        }

        JaxRsSubResourceLocator locator = new JaxRsSubResourceLocator(resourceClass, method);
        ClassInfo subResourceClass = context.getIndex().getClassByName(methodReturnType.name());

        // Do not allow the same resource locator method to be used twice (sign of infinite recursion)
        if (subResourceClass != null && !this.subResourceStack.contains(locator)) {
            Function<AnnotationInstance, Parameter> reader = t -> context.io().parameterIO().read(t);

            ResourceParameters params = JaxRsParameterProcessor.process(context, currentAppPath, resourceClass, method,
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

            // Check for @Tags from the method or the subresource class, default to the parent's tags
            Set<String> subresourceTags = Optional.ofNullable(processTags(context, method, openApi, true))
                    .orElseGet(() -> Optional.ofNullable(processTags(context, subResourceClass, openApi, true))
                            .orElse(tagRefs));

            /*
             * Combine parameters passed previously with all of those from the current resource class and
             * method that apply to this Path. The full list will be used as PATH-LEVEL parameters for
             * sub-resource methods deeper in the scan.
             */
            processResourceClass(openApi, subResourceClass,
                    ListUtil.mergeNullableLists(locatorPathParameters,
                            params.getPathItemParameters(),
                            params.getOperationParameters()),
                    subresourceTags);

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
    private void processResourceMethod(final ClassInfo resourceClass,
            final MethodInfo method,
            final PathItem.HttpMethod methodType,
            Set<String> resourceTags,
            List<Parameter> locatorPathParameters,
            Map<DotName, Map<String, APIResponse>> exceptionResponseMap) {

        JaxRsLogging.log.processingMethod(method.toString());

        // Figure out the current @Produces and @Consumes (if any)
        String[] defaultConsumes = getDefaultConsumes(context, method, getResourceParameters(resourceClass, method));
        context.setDefaultConsumes(defaultConsumes);
        context.setCurrentConsumes(getMediaTypes(method, JaxRsConstants.CONSUMES, defaultConsumes).orElse(null));

        String[] defaultProduces = getDefaultProduces(context, method);
        context.setDefaultProduces(defaultProduces);
        context.setCurrentProduces(getMediaTypes(method, JaxRsConstants.PRODUCES, defaultProduces).orElse(null));

        // Process any @Operation annotation
        Optional<Operation> maybeOperation = processOperation(context, resourceClass, method);
        if (!maybeOperation.isPresent()) {
            return; // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
        }
        final Operation operation = maybeOperation.get();

        // Process tags - @Tag and @Tags annotations combines with the resource tags we've already found (passed in)
        processOperationTags(context, method, context.getOpenApi(), resourceTags, operation);

        // Process @Parameter annotations.
        ResourceParameters params = getResourceParameters(resourceClass, method);
        List<Parameter> operationParams = params.getOperationParameters();
        operation.setParameters(operationParams);
        if (locatorPathParameters != null && operationParams != null) {
            locatorPathParameters = excludeOperationParameters(locatorPathParameters, operationParams);
        }

        PathItem pathItem = OASFactory.createPathItem();
        pathItem.setParameters(ListUtil.mergeNullableLists(locatorPathParameters, params.getPathItemParameters()));

        // Process any @RequestBody annotation (note: the @RequestBody annotation can be found on a method argument *or* on the method)
        RequestBody requestBody = processRequestBody(context, method, params);
        if (requestBody != null) {
            operation.setRequestBody(requestBody);
        }

        // Process @APIResponse annotations
        processResponse(context, resourceClass, method, operation, exceptionResponseMap);

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

    private ResourceParameters getResourceParameters(final ClassInfo resourceClass, final MethodInfo method) {
        Function<AnnotationInstance, Parameter> reader = t -> context.io().parameterIO().read(t);
        return JaxRsParameterProcessor.process(context, currentAppPath, resourceClass, method,
                reader, context.getExtensions());
    }

    /**
     * Remove from the list of locator parameters and parameter present in the list of operation parameters.
     * Parameters are considered the same if they have the same value for name and {@code in}.
     *
     * @param locatorParams list of locator parameters
     * @param operationParams list of operation parameters
     * @return
     */
    static List<Parameter> excludeOperationParameters(List<Parameter> locatorParams, List<Parameter> operationParams) {
        return locatorParams.stream()
                .filter(param -> operationParams.stream()
                        .noneMatch(oParam -> Objects.equals(param.getName(), oParam.getName()) &&
                                Objects.equals(param.getIn(), oParam.getIn())))
                .collect(Collectors.toList());
    }

    /**
     * Search for {@code annotationName} on {@code resourceMethod} or any of the methods it overrides. If
     * not found, search for {@code annotationName} on {@code resourceMethod}'s containing class or any
     * of its super-classes or interfaces.
     */
    Optional<String[]> getMediaTypes(MethodInfo resourceMethod,
            Set<DotName> annotationName, String[] defaultValue) {

        return context.getAugmentedIndex().ancestry(resourceMethod).entrySet()
                .stream()
                .map(e -> getMediaTypeAnnotation(e.getKey(), e.getValue(), annotationName))
                .filter(Objects::nonNull)
                .map(annotation -> mediaTypeValue(annotation, defaultValue))
                .findFirst();
    }

    AnnotationInstance getMediaTypeAnnotation(ClassInfo clazz, MethodInfo method, Set<DotName> annotationName) {
        AnnotationInstance annotation = null;

        if (method != null) {
            annotation = context.annotations().getAnnotation(method, annotationName);
        }

        if (annotation == null) {
            annotation = context.annotations().getAnnotation(clazz, annotationName);
        }

        return annotation;
    }

    static String[] mediaTypeValue(AnnotationInstance mediaTypeAnnotation, String[] defaultValue) {
        AnnotationValue annotationValue = mediaTypeAnnotation.value();

        if (annotationValue != null) {
            return flattenAndTrimMediaTypes(annotationValue.asStringArray());
        }

        return defaultValue;
    }

    /**
     * Flattens and trims the list of media types from a {@code @Consumes} or {@code @Produces} annotation.
     * <p>
     * E.g. Converts {@code ["foo, bar"," baz ","qux"]} to {@code ["foo","bar","baz","qux"]}
     *
     * @param mediaTypes array of media types which may contain comma separated values
     * @return elements of {@code mediaTypes} trimmed and with any comma separated values converted into separate elements
     */
    static String[] flattenAndTrimMediaTypes(String[] mediaTypes) {
        return Arrays.stream(mediaTypes)
                .map(mediaType -> mediaType.split(","))
                .flatMap(Arrays::stream)
                .map(String::trim)
                .toArray(String[]::new);
    }

    /**
     * Use the Jandex index to find all jax-rs resource classes. This is done by searching for
     * all Class-level @Path annotations.
     *
     * @param context current scanning context
     * @return Collection of ClassInfo's
     */
    private Collection<ClassInfo> getJaxRsResourceClasses() {
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
                .filter(this::hasImplementationOrIsIncluded)
                .collect(Collectors.toCollection(() -> new TreeSet<ClassInfo>((one, two) -> one.name().compareTo(two.name())))); // CompositeIndex instances may return duplicates

    }

    /**
     * Use the Jandex index to find all resource classes specified in the configuration.
     *
     * @param context current scanning context
     * @return Collection of ClassInfo's
     */
    private Collection<ClassInfo> getConfigurationResourceClasses() {
        return context.getConfig().getScanResourceClasses()
                .keySet()
                .stream()
                .map(DotName::createSimple)
                .map(className -> context.getIndex().getClassByName(className))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean hasImplementationOrIsIncluded(ClassInfo clazz) {
        if (neitherAbstractNorSyntheticRestClient(clazz)) {
            return true;
        }

        FilteredIndexView filteredIndex = context.getIndex();

        if (filteredIndex.getAllKnownImplementors(clazz.name()).stream().anyMatch(this::neitherAbstractNorSynthetic)) {
            return true;
        }

        return filteredIndex.explicitlyAccepts(clazz.name());
    }

    /**
     * Determine whether the provided ClassInfo is an abstract class/interface or a synthetic implementation
     * of an interface annotated with {@code org.eclipse.microprofile.rest.client.inject.RegisterRestClient}.
     *
     * @return {@code true} if the class is neither an interface nor a synthetic (generated) class annotated with
     *         {@code org.eclipse.microprofile.rest.client.inject.RegisterRestClient}, otherwise {@code false}.
     */
    private boolean neitherAbstractNorSyntheticRestClient(ClassInfo clazz) {
        if (Modifier.isAbstract(clazz.flags())) {
            return false;
        }

        if (!clazz.isSynthetic()) {
            return true;
        }

        final AugmentedIndexView index = context.getAugmentedIndex();

        return index.inheritanceChain(clazz, Type.create(clazz.name(), Type.Kind.CLASS))
                .entrySet()
                .stream()
                .flatMap(e -> index.interfaces(e.getKey()).stream())
                .map(index::getClass)
                .filter(Objects::nonNull)
                .noneMatch(iface -> context.annotations().getAnnotation(iface, JaxRsConstants.REGISTER_REST_CLIENT) != null);
    }

    private boolean neitherAbstractNorSynthetic(ClassInfo clazz) {
        return !Modifier.isAbstract(clazz.flags()) && !clazz.isSynthetic();
    }
}
