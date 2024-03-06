package io.smallrye.openapi.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiRuntimeException;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.JsonIO;
import io.smallrye.openapi.runtime.io.OpenAPIDefinitionIO;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerFactory;

@SuppressWarnings("deprecation")
public class SmallRyeOpenAPI {

    private final OpenAPI model;
    private final Object jsonModel;
    private final BiFunction<? super Object, Format, String> toString;

    @SuppressWarnings("unchecked")
    private SmallRyeOpenAPI(OpenAPI model, Object jsonModel, BiFunction<?, Format, String> toString) {
        this.model = model;
        this.jsonModel = jsonModel;
        this.toString = (BiFunction<? super Object, Format, String>) toString;
    }

    public OpenAPI model() {
        return model;
    }

    public String toJSON() {
        return toString.apply(jsonModel, Format.JSON);
    }

    public String toYAML() {
        return toString.apply(jsonModel, Format.YAML);
    }

    public static SmallRyeOpenAPI.Builder builder() {
        return new SmallRyeOpenAPI.Builder();
    }

    public static class Builder {

        private Config config;
        private ClassLoader applicationClassLoader;
        private OpenAPI initialModel;

        private boolean enableModelReader = true;

        private boolean enableStandardStaticFiles = true;
        private Function<String, URL> resourceLocator;
        private InputStream customStaticFile;

        private boolean defaultRequiredProperties = true;

        private IndexView index;
        private boolean enableAnnotationScan = true;
        private ClassLoader scannerClassLoader;
        private Predicate<URL> staticFileFilter = f -> true;
        private Predicate<String> scannerFilter = n -> true;

        private Function<Collection<ClassInfo>, String> contextRootResolver = apps -> null;
        private UnaryOperator<Type> typeConverter = UnaryOperator.identity();
        private Function<String, Object> jsonParser = null;
        private Function<String, Schema> schemaParser = null;

        private boolean enableStandardFilter = true;
        private Map<String, OASFilter> filters = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder withConfig(Config config) {
            this.config = Objects.requireNonNull(config);
            return this;
        }

        public Builder withApplicationClassLoader(ClassLoader classLoader) {
            this.applicationClassLoader = Objects.requireNonNull(classLoader);
            return this;
        }

        public Builder withInitialModel(OpenAPI initialModel) {
            this.initialModel = initialModel;
            return this;
        }

        public Builder enableModelReader(boolean enableModelReader) {
            this.enableModelReader = enableModelReader;
            return this;
        }

        public Builder enableStandardStaticFiles(boolean enableStandardStaticFiles) {
            this.enableStandardStaticFiles = enableStandardStaticFiles;
            return this;
        }

        public Builder enableStandardFilter(boolean enableStandardFilter) {
            this.enableStandardFilter = enableStandardFilter;
            return this;
        }

        public Builder defaultRequiredProperties(boolean defaultRequiredProperties) {
            this.defaultRequiredProperties = defaultRequiredProperties;
            return this;
        }

        public Builder withResourceLocator(Function<String, URL> resourceLocator) {
            this.resourceLocator = resourceLocator;
            return this;
        }

        public Builder withCustomStaticFile(InputStream customStaticFile) {
            this.customStaticFile = customStaticFile;
            return this;
        }

        public Builder setStaticFileFilter(Predicate<URL> staticFileFilter) {
            this.staticFileFilter = Objects.requireNonNull(staticFileFilter);
            return this;
        }

        public Builder withIndex(IndexView index) {
            this.index = Objects.requireNonNull(index);
            return this;
        }

        public Builder withContextRootResolver(Function<Collection<ClassInfo>, String> contextRootResolver) {
            this.contextRootResolver = Objects.requireNonNull(contextRootResolver);
            return this;
        }

        public Builder withTypeConverter(UnaryOperator<Type> typeConverter) {
            this.typeConverter = Objects.requireNonNull(typeConverter);
            return this;
        }

        public Builder withJsonParser(Function<String, Object> jsonParser) {
            this.jsonParser = jsonParser;
            return this;
        }

        public Builder withSchemaParser(Function<String, Schema> schemaParser) {
            this.schemaParser = schemaParser;
            return this;
        }

        public Builder enableAnnotationScan(boolean enableAnnotationScan) {
            this.enableAnnotationScan = enableAnnotationScan;
            return this;
        }

        public Builder withScannerClassLoader(ClassLoader scannerClassLoader) {
            this.scannerClassLoader = scannerClassLoader;
            return this;
        }

        public Builder withScannerFilter(Predicate<String> scannerFilter) {
            this.scannerFilter = Objects.requireNonNull(scannerFilter);
            return this;
        }

        public Builder withFilters(Collection<OASFilter> filters) {
            Objects.requireNonNull(filters);
            this.filters.clear();
            filters.forEach(filter -> this.filters.put(filter.getClass().getName(), filter));
            return this;
        }

        public Builder withFilterNames(Collection<String> filterNames) {
            Objects.requireNonNull(filterNames);
            filters.clear();
            filterNames.forEach(filter -> this.filters.put(filter, null));
            return this;
        }

        public Builder addFilter(OASFilter filter) {
            Objects.requireNonNull(filter);
            filters.put(filter.getClass().getName(), filter);
            return this;
        }

        public Builder addFilterName(String filterName) {
            Objects.requireNonNull(filterName);
            filters.put(filterName, null);
            return this;
        }

        public <V, A extends V, O extends V, AB, OB> SmallRyeOpenAPI build() {
            ClassLoader appClassLoader = applicationClassLoader != null ? applicationClassLoader
                    : Thread.currentThread().getContextClassLoader();

            OpenApiConfig buildConfig = OpenApiConfig.fromConfig(this.config);
            IOContext<V, A, O, AB, OB> io = IOContext.forJson(JsonIO.newInstance(buildConfig));
            OpenAPIDefinitionIO<V, A, O, AB, OB> modelIO = new OpenAPIDefinitionIO<>(io);
            FilteredIndexView filteredIndex = new FilteredIndexView(index, buildConfig);

            OpenAPI readerModel = null;
            OpenAPI staticModel = null;
            OpenAPI annotationModel = null;
            OASFilter standardFilter = null;

            if (enableModelReader) {
                readerModel = OpenApiProcessor.modelFromReader(buildConfig, appClassLoader, filteredIndex);
                debugModel("reader", readerModel);
            }

            if (enableStandardStaticFiles) {
                Function<String, URL> loadFn = Optional.ofNullable(resourceLocator)
                        .orElse(appClassLoader::getResource);

                staticModel = OpenApiProcessor.loadOpenApiStaticFiles(loadFn)
                        .stream()
                        .filter(file -> staticFileFilter.test(file.getLocator()))
                        .map(file -> {
                            try (Reader reader = new InputStreamReader(file.getContent())) {
                                V dom = io.jsonIO().fromReader(reader, file.getFormat());
                                OpenAPI fileModel = modelIO.readValue(dom);
                                debugModel("static file", fileModel);
                                return fileModel;
                            } catch (IOException e) {
                                throw new OpenApiRuntimeException("IOException reading " + file.getFormat() + " static file", e);
                            }
                        })
                        .reduce(MergeUtil::merge)
                        .orElse(null);
            }

            if (customStaticFile != null) {
                try (Reader reader = new InputStreamReader(customStaticFile)) {
                    V dom = io.jsonIO().fromReader(reader);
                    OpenAPI customStaticModel = modelIO.readValue(dom);
                    debugModel("static file", customStaticModel);
                    staticModel = MergeUtil.merge(customStaticModel, staticModel);
                } catch (IOException e) {
                    throw new OpenApiRuntimeException("IOException reading custom static file", e);
                }
            }

            if (enableAnnotationScan && !buildConfig.scanDisable()) {
                AnnotationScannerExtension ext = newExtension(modelIO);
                AnnotationScannerContext scannerContext = new AnnotationScannerContext(filteredIndex, appClassLoader,
                        Collections.singletonList(ext), false, buildConfig, modelIO, new OpenAPIImpl());
                io.scannerContext(scannerContext);
                Supplier<Iterable<AnnotationScanner>> supplier = Optional.ofNullable(scannerClassLoader)
                        .map(AnnotationScannerFactory::new)
                        .orElseGet(() -> new AnnotationScannerFactory(appClassLoader));
                OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(scannerContext, supplier);
                annotationModel = scanner.scan(scannerFilter);
                debugModel("annotation", annotationModel);
            }

            if (enableStandardFilter) {
                standardFilter = OpenApiProcessor.getFilter(buildConfig, appClassLoader, filteredIndex);
            }

            OpenApiDocument doc = OpenApiDocument.newInstance();
            doc.config(buildConfig);
            doc.defaultRequiredProperties(defaultRequiredProperties);
            doc.modelFromReader(MergeUtil.merge(initialModel, readerModel));
            doc.modelFromStaticFile(staticModel);
            doc.modelFromAnnotations(annotationModel);

            filters.entrySet()
                    .stream()
                    .map(e -> Optional.ofNullable(e.getValue())
                            // Create an instance from the key (class name) when the value is null
                            .orElseGet(() -> OpenApiProcessor.getFilter(e.getKey(), appClassLoader, filteredIndex)))
                    .forEach(doc::filter);

            if (standardFilter != null && !filters.containsKey(standardFilter.getClass().getName())) {
                doc.filter(standardFilter);
            }

            doc.initialize();

            OpenAPI model = doc.get();
            BiFunction<V, Format, String> toString = io.jsonIO()::toString;
            return new SmallRyeOpenAPI(model, modelIO.write(model).orElse(null), toString);
        }

        private <V, A extends V, O extends V, AB, OB> AnnotationScannerExtension newExtension(
                OpenAPIDefinitionIO<V, A, O, AB, OB> modelIO) {
            return new AnnotationScannerExtension() {
                @Override
                public void processScannerApplications(AnnotationScanner scanner, Collection<ClassInfo> applications) {
                    Optional.ofNullable(contextRootResolver.apply(applications))
                            .ifPresent(scanner::setContextRoot);
                }

                @Override
                public Type resolveAsyncType(Type type) {
                    return typeConverter.apply(type);
                }

                @Override
                public Object parseValue(String value) {
                    return Optional.ofNullable(jsonParser)
                            .map(parser -> parser.apply(value))
                            .orElseGet(() -> modelIO.jsonIO().parseValue(value));
                }

                @Override
                public Schema parseSchema(String jsonSchema) {
                    return Optional.ofNullable(schemaParser)
                            .map(parser -> parser.apply(jsonSchema))
                            .orElseGet(() -> {
                                V schemaModel = modelIO.jsonIO().fromString(jsonSchema, Format.JSON);
                                return modelIO.schemas().readValue(schemaModel);
                            });
                }
            };
        }

        private void debugModel(String source, OpenAPI model) {
            if (model == null) {
                return;
            }
            ApiLogging.logger.addingModel(source);
            debugMap("callbacks", source, Optional.ofNullable(model.getComponents()).map(Components::getCallbacks));
            debugMap("examples", source, Optional.ofNullable(model.getComponents()).map(Components::getExamples));
            debugMap("headers", source, Optional.ofNullable(model.getComponents()).map(Components::getHeaders));
            debugMap("links", source, Optional.ofNullable(model.getComponents()).map(Components::getLinks));
            debugMap("parameters", source, Optional.ofNullable(model.getComponents()).map(Components::getParameters));
            debugMap("request bodies", source, Optional.ofNullable(model.getComponents()).map(Components::getRequestBodies));
            debugMap("responses", source, Optional.ofNullable(model.getComponents()).map(Components::getResponses));
            debugMap("schemas", source, Optional.ofNullable(model.getComponents()).map(Components::getSchemas));
            debugMap("security schemes", source,
                    Optional.ofNullable(model.getComponents()).map(Components::getSecuritySchemes));
            debugList("servers", source, Optional.ofNullable(model.getServers()));
            debugMap("path items", source, Optional.ofNullable(model.getPaths()).map(Paths::getPathItems));
            debugList("security", source, Optional.ofNullable(model.getSecurity()));
            debugList("tags", source, Optional.ofNullable(model.getTags()));
            debugMap("extensions", source, Optional.ofNullable(model.getExtensions()));
        }

        private void debugMap(String name, String source, Optional<Map<?, ?>> collection) {
            debugModel(name, source, collection.map(Map::size));
        }

        private void debugList(String name, String source, Optional<Collection<?>> collection) {
            debugModel(name, source, collection.map(Collection::size));
        }

        private void debugModel(String name, String source, Optional<Integer> collection) {
            ApiLogging.logger.addingModel(name, source, collection.map(Object::toString).orElse("<no>"));
        }
    }
}
