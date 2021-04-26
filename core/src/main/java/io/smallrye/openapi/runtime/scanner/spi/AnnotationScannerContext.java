package io.smallrye.openapi.runtime.scanner.spi;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.UnaryOperator;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;
import io.smallrye.openapi.runtime.scanner.dataobject.IgnoreResolver;
import io.smallrye.openapi.runtime.scanner.dataobject.PropertyNamingStrategyFactory;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;

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

    public AnnotationScannerContext(FilteredIndexView index, ClassLoader classLoader,
            List<AnnotationScannerExtension> extensions,
            OpenApiConfig config,
            OpenAPI openApi) {
        this.index = index;
        this.augmentedIndex = AugmentedIndexView.augment(index);
        this.ignoreResolver = new IgnoreResolver(this.augmentedIndex);
        this.classLoader = classLoader;
        this.extensions = extensions;
        this.config = config;
        this.openApi = openApi;
        this.propertyNameTranslator = PropertyNamingStrategyFactory.getStrategy(config.propertyNamingStrategy(), classLoader);
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

}
