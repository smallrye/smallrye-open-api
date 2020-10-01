package io.smallrye.openapi.runtime.scanner.spi;

import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.IndexView;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;

/**
 * Context for scanners.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotationScannerContext {
    private final FilteredIndexView index;
    private final AugmentedIndexView augmentedIndex;
    private final List<AnnotationScannerExtension> extensions;
    private final OpenApiConfig config;
    private final ClassLoader classLoader;
    private final OpenAPI openApi;

    public AnnotationScannerContext(FilteredIndexView index, ClassLoader classLoader,
            List<AnnotationScannerExtension> extensions,
            OpenApiConfig config,
            OpenAPI openApi) {
        this.index = index;
        this.augmentedIndex = AugmentedIndexView.augment(index);
        this.classLoader = classLoader;
        this.extensions = extensions;
        this.config = config;
        this.openApi = openApi;
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

    public List<AnnotationScannerExtension> getExtensions() {
        return extensions;
    }

    public OpenApiConfig getConfig() {
        return config;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public OpenAPI getOpenApi() {
        return openApi;
    }
}
