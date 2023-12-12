package io.smallrye.openapi.runtime.scanner.spi;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;
import io.smallrye.openapi.runtime.scanner.dataobject.BeanValidationScanner;
import io.smallrye.openapi.runtime.scanner.dataobject.IgnoreResolver;
import io.smallrye.openapi.runtime.scanner.dataobject.PropertyNamingStrategyFactory;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.scanner.processor.JavaSecurityProcessor;
import io.smallrye.openapi.runtime.util.Annotations;

/**
 * Context for scanners.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotationScannerContext {

    private final FilteredIndexView index;
    private final AugmentedIndexView augmentedIndex;
    private final IgnoreResolver ignoreResolver;
    private final List<AnnotationScannerExtension> extensions;
    private final OpenApiConfig config;
    private final UnaryOperator<String> propertyNameTranslator;
    private final ClassLoader classLoader;
    private final OpenAPI openApi;
    private final Deque<Type> scanStack = new ArrayDeque<>();
    private Deque<TypeResolver> resolverStack = new ArrayDeque<>();
    private final Optional<BeanValidationScanner> beanValidationScanner;
    private final Set<Type> jsonViews = new LinkedHashSet<>();
    private String[] currentConsumes;
    private String[] currentProduces;
    private String[] defaultConsumes;
    private String[] defaultProduces;
    private Optional<AnnotationScanner> currentScanner = Optional.empty();
    private final SchemaRegistry schemaRegistry;
    private final JavaSecurityProcessor javaSecurityProcessor;
    private final Annotations annotations;

    private final Map<String, MethodInfo> operationIdMap = new HashMap<>();

    public AnnotationScannerContext(FilteredIndexView index, ClassLoader classLoader,
            List<AnnotationScannerExtension> extensions,
            OpenApiConfig config,
            OpenAPI openApi) {
        this.index = index;
        this.augmentedIndex = AugmentedIndexView.augment(index);
        this.ignoreResolver = new IgnoreResolver(this);
        this.classLoader = classLoader;
        this.extensions = extensions;
        this.config = config;
        this.openApi = openApi;
        this.propertyNameTranslator = PropertyNamingStrategyFactory.getStrategy(config.propertyNamingStrategy(), classLoader);
        this.beanValidationScanner = config.scanBeanValidation() ? Optional.of(new BeanValidationScanner(this))
                : Optional.empty();
        this.schemaRegistry = new SchemaRegistry(this);
        this.javaSecurityProcessor = new JavaSecurityProcessor(this);
        this.annotations = new Annotations(this);
    }

    public AnnotationScannerContext(IndexView index, ClassLoader classLoader,
            OpenApiConfig config) {
        this(new FilteredIndexView(index, config), classLoader, Collections.emptyList(), config, new OpenAPIImpl());
    }

    public FilteredIndexView getIndex() {
        return index;
    }

    public AugmentedIndexView getAugmentedIndex() {
        return augmentedIndex;
    }

    public IgnoreResolver getIgnoreResolver() {
        return ignoreResolver;
    }

    public List<AnnotationScannerExtension> getExtensions() {
        return extensions;
    }

    public OpenApiConfig getConfig() {
        return config;
    }

    public UnaryOperator<String> getPropertyNameTranslator() {
        return propertyNameTranslator;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public OpenAPI getOpenApi() {
        return openApi;
    }

    public Deque<Type> getScanStack() {
        return scanStack;
    }

    public Deque<TypeResolver> getResolverStack() {
        return resolverStack;
    }

    public TypeResolver getResourceTypeResolver() {
        return resolverStack.peek();
    }

    public Optional<BeanValidationScanner> getBeanValidationScanner() {
        return beanValidationScanner;
    }

    public Set<Type> getJsonViews() {
        return jsonViews;
    }

    public Map<String, MethodInfo> getOperationIdMap() {
        return operationIdMap;
    }

    public String[] getCurrentConsumes() {
        return currentConsumes;
    }

    public void setCurrentConsumes(String[] currentConsumes) {
        this.currentConsumes = currentConsumes;
    }

    public String[] getCurrentProduces() {
        return currentProduces;
    }

    public void setCurrentProduces(String[] currentProduces) {
        this.currentProduces = currentProduces;
    }

    public String[] getDefaultConsumes() {
        return defaultConsumes;
    }

    public void setDefaultConsumes(String[] defaultConsumes) {
        this.defaultConsumes = defaultConsumes;
    }

    public String[] getDefaultProduces() {
        return defaultProduces;
    }

    public void setDefaultProduces(String[] defaultProduces) {
        this.defaultProduces = defaultProduces;
    }

    public Optional<AnnotationScanner> getCurrentScanner() {
        return currentScanner;
    }

    public void setCurrentScanner(AnnotationScanner currentScanner) {
        this.currentScanner = Optional.ofNullable(currentScanner);
    }

    public SchemaRegistry getSchemaRegistry() {
        return schemaRegistry;
    }

    public JavaSecurityProcessor getJavaSecurityProcessor() {
        return javaSecurityProcessor;
    }

    public Annotations annotations() {
        return annotations;
    }
}
