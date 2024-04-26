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
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
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

    /**
     * The final {@link OpenAPI} model generated from {@link Builder#build()}.
     */
    public OpenAPI model() {
        return model;
    }

    /**
     * Serializes the {@link #model} as a JSON String
     */
    public String toJSON() {
        return toString.apply(jsonModel, Format.JSON);
    }

    /**
     * Serializes the {@link #model} as a YAML String
     */
    public String toYAML() {
        return toString.apply(jsonModel, Format.YAML);
    }

    /**
     * Create a new {@link Builder} instance;
     *
     * @return a new {@link Builder}
     */
    public static SmallRyeOpenAPI.Builder builder() {
        return new SmallRyeOpenAPI.Builder();
    }

    public static class Builder {

        private static final IndexView EMPTY_INDEX = new Indexer().complete();
        private Config config;
        private ClassLoader applicationClassLoader;
        private OpenAPI initialModel;

        private boolean enableModelReader = true;

        private boolean enableStandardStaticFiles = true;
        private Function<String, URL> resourceLocator;
        private Supplier<InputStream> customStaticFile = () -> null;

        private boolean defaultRequiredProperties = true;

        private IndexView index = EMPTY_INDEX;
        private boolean enableAnnotationScan = true;
        private boolean enableUnannotatedPathParameters = false;
        private ClassLoader scannerClassLoader;
        private Predicate<String> scannerFilter = n -> true;

        private Function<Collection<ClassInfo>, String> contextRootResolver = apps -> null;
        private UnaryOperator<Type> typeConverter = UnaryOperator.identity();
        private Function<String, Object> jsonParser = null;
        private Function<String, Schema> schemaParser = null;

        private boolean enableStandardFilter = true;
        private Map<String, OASFilter> filters = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Set the MicroProfile Config to be used when building the OpenAPI
         * model. When not set, the builder will obtain a Config instance using
         * {@link ConfigProvider#getConfig()} with the
         * {@linkplain #withApplicationClassLoader(ClassLoader) application
         * class loader}.
         *
         * @param config
         *        Config instance, nulls not allowed
         * @return this builder
         */
        public Builder withConfig(Config config) {
            this.config = Objects.requireNonNull(config);
            return this;
        }

        /**
         * Set the application ClassLoader to be used when building the OpenAPI
         * model.
         *
         * @param classLoader
         *        ClassLoader instance, nulls not allowed
         * @return this builder
         */
        public Builder withApplicationClassLoader(ClassLoader classLoader) {
            this.applicationClassLoader = Objects.requireNonNull(classLoader);
            return this;
        }

        /**
         * Set an initial model used when building the OpenAPI model. The
         * elements in this model will be overridden by any conflicting elements
         * in models generated by the builder.
         *
         * @param initialModel
         *        initial OpenAPI model
         * @return this builder
         */
        public Builder withInitialModel(OpenAPI initialModel) {
            this.initialModel = initialModel;
            return this;
        }

        /**
         * Enable (true) or disable (false) the lookup and use of an
         * OASModelReader. Default is true.
         *
         * @param enableModelReader
         *        true if the model reader should be loaded and called,
         *        otherwise false.
         * @return this builder
         */
        public Builder enableModelReader(boolean enableModelReader) {
            this.enableModelReader = enableModelReader;
            return this;
        }

        /**
         * Enable (true) or disable (false) the lookup and use of the standard
         * static OpenAPI files (e.g. {@code META-INF/openapi.(json|yaml|yml)}.
         * Default is true.
         *
         * @param enableStandardStaticFiles
         *        true if the standard static files should be loaded and
         *        included, otherwise false.
         * @return this builder
         */
        public Builder enableStandardStaticFiles(boolean enableStandardStaticFiles) {
            this.enableStandardStaticFiles = enableStandardStaticFiles;
            return this;
        }

        /**
         * Enable (true) or disable (false) the lookup and use of the standard
         * OASFilter. Default is true.
         *
         * @param enableStandardFilter
         *        true if the filter should be loaded and called, otherwise
         *        false.
         * @return this builder
         */
        public Builder enableStandardFilter(boolean enableStandardFilter) {
            this.enableStandardFilter = enableStandardFilter;
            return this;
        }

        /**
         * Enable (true) or disable (false) setting default values for the
         * OpenAPI properties listed below. Default is true.
         *
         * <ul>
         * <li>Create an empty {@code paths} object if none specified
         * <li>Set a generated value for {@code info.title} if none specified
         * <li>Set a generated value for {@code info.version} if none specified
         * <li>Set a default value for {@code openapi} (the specification
         * version) if none specified. E.g. 3.0.3
         * </ul>
         *
         * @param defaultRequiredProperties
         *        true if default values should be set when necessary,
         *        otherwise false.
         * @return this builder
         */
        public Builder defaultRequiredProperties(boolean defaultRequiredProperties) {
            this.defaultRequiredProperties = defaultRequiredProperties;
            return this;
        }

        /**
         * Provide a resource locator function that when given a String path for
         * a static file will return a URL that may be used to load the
         * resource. This function is intended to support environments where the
         * resource is not on the class path and an alternate loading approach
         * is necessary, for example using a ServletContext.
         *
         * @param resourceLocator
         * @return this builder
         */
        public Builder withResourceLocator(Function<String, URL> resourceLocator) {
            this.resourceLocator = resourceLocator;
            return this;
        }

        /**
         * Provide a supplier of a custom static file that is not one of those in the standard locations
         * or with a standard name. The user of this builder is responsible for closing the stream
         * after {@link #build() build} has been invoked. Repeated calls to {@link #build() build} must
         * ensure the supplied stream is open and readable.
         *
         * @param customStaticFile a supplier of an input stream used to read a custom static file, null not allowed
         * @return this builder
         */
        public Builder withCustomStaticFile(Supplier<InputStream> customStaticFile) {
            this.customStaticFile = Objects.requireNonNull(customStaticFile);
            return this;
        }

        /**
         * Provide an IndexView for use in annotation scanning.
         *
         * @param index a Jandex IndexView for use by the annotation scanner, null not allowed
         * @return this builder
         */
        public Builder withIndex(IndexView index) {
            this.index = Objects.requireNonNull(index);
            return this;
        }

        /**
         * Provide function that when given the collection of all known Jakarta REST Application ClassInfo
         * instances, resolves the context root (path prefix) to be applied to all paths in the OpenAPI
         * document. If the function returns a null value, the context root is not set.
         * <p>
         * The function is only used during annotation scanning.
         *
         * @param contextRootResolver a function to return the context root (global path prefix) for the OpenAPI document, null
         *        not allowed
         * @return this builder
         */
        public Builder withContextRootResolver(Function<Collection<ClassInfo>, String> contextRootResolver) {
            this.contextRootResolver = Objects.requireNonNull(contextRootResolver);
            return this;
        }

        /**
         * Provide a function that when given a Jandex Type, returns an alternate type if necessary. This
         * method may be used by platforms that require special type unwrapping or conversion functionality.
         * <p>
         * The function is only used during annotation scanning.
         *
         * @param typeConverter a function to convert a type to another type, null not allowed
         * @return this builder
         */
        public Builder withTypeConverter(UnaryOperator<Type> typeConverter) {
            this.typeConverter = Objects.requireNonNull(typeConverter);
            return this;
        }

        /**
         * Provide a function that when given a JSON-formatted string, returns a parsed Java object
         * equivalent. The parsed object should be limited to Java Lists, Maps, and basic terminal
         * types such as Java primitives, Numbers, and Strings.
         * <p>
         * The function is only used during annotation scanning.
         *
         * @param jsonParser a function to convert JSON string to a Java object
         * @return this builder
         */
        public Builder withJsonParser(Function<String, Object> jsonParser) {
            this.jsonParser = jsonParser;
            return this;
        }

        /**
         * Provide a function that when given a JSON-formatted schema, returns a MicroProfile
         * OpenAPI {@link Schema} instance.
         * <p>
         * The function is only used during annotation scanning.
         *
         * @param schemaParser a function to convert a JSON string to a Schema instance
         * @return this builder
         */
        public Builder withSchemaParser(Function<String, Schema> schemaParser) {
            this.schemaParser = schemaParser;
            return this;
        }

        /**
         * Enable (true) or disable (false) annotation scanning. Default is true.
         *
         * @param enableAnnotationScan
         *        true if annotation scanning is enabled, otherwise false.
         * @return this builder
         */
        public Builder enableAnnotationScan(boolean enableAnnotationScan) {
            this.enableAnnotationScan = enableAnnotationScan;
            return this;
        }

        /**
         * Enable (true) or disable (false) path parameters to be optionally annotated.
         *
         * Default is false.
         *
         * @param enableUnannotatedPathParameters
         *        true if annotation use on path parameters is optional, otherwise false.
         * @return this builder
         */
        public Builder enableUnannotatedPathParameters(boolean enableUnannotatedPathParameters) {
            this.enableUnannotatedPathParameters = enableUnannotatedPathParameters;
            return this;
        }

        /**
         * Provide a class loader used to load AnnotationScanner instances via
         * the {@link ServiceLoader}. If not set, instances will be loaded using the
         * {@linkplain #withApplicationClassLoader(ClassLoader) application
         * class loader}, or the {@linkplain Thread#getContextClassLoader()
         * context class loader} of the thread used to invoke
         * {@link #build() build}.
         * <p>
         * The function is only used during annotation scanning.
         *
         * @param scannerClassLoader
         *        class loader used for loading AnnotationScanners
         * @return this builder
         */
        public Builder withScannerClassLoader(ClassLoader scannerClassLoader) {
            this.scannerClassLoader = scannerClassLoader;
            return this;
        }

        /**
         * Provide a filter predicate used to include/exclude AnnotationScaner instances
         * found via the {@link ServiceLoader}.
         *
         * @param scannerFilter
         * @return this builder
         */
        public Builder withScannerFilter(Predicate<String> scannerFilter) {
            this.scannerFilter = Objects.requireNonNull(scannerFilter);
            return this;
        }

        /**
         * Provide a collection of OASFilter instances to apply to the final OpenAPI model. The
         * filters will be executed in the same order as given in the collection.
         *
         * @param filters collection of OASFilter instances
         * @return this builder
         */
        public Builder withFilters(Collection<OASFilter> filters) {
            Objects.requireNonNull(filters);
            this.filters.clear();
            filters.forEach(filter -> this.filters.put(filter.getClass().getName(), filter));
            return this;
        }

        /**
         * Provide a collection of OASFilter implementation class names to apply to the final OpenAPI model. New
         * instances of the named classes will be instantiated at run time using the
         * {@linkplain #withApplicationClassLoader(ClassLoader) application class loader}
         * provided, or the {@linkplain Thread#getContextClassLoader()
         * context class loader} of the thread used to invoke
         * {@link #build() build}.
         *
         * The filters will be executed in the same order as given in the collection.
         *
         * @param filterNames collection of OASFilter implementation class names
         * @return this builder
         */
        public Builder withFilterNames(Collection<String> filterNames) {
            Objects.requireNonNull(filterNames);
            filters.clear();
            filterNames.forEach(filter -> this.filters.put(filter, null));
            return this;
        }

        /**
         * Add an OASFilter instances to apply to the final OpenAPI model. Filters will be executed
         * in the order they are added.
         *
         * @param filter OASFilter instance
         * @return this builder
         */
        public Builder addFilter(OASFilter filter) {
            Objects.requireNonNull(filter);
            filters.put(filter.getClass().getName(), filter);
            return this;
        }

        /**
         * Add an OASFilter implementation class name to apply to the final OpenAPI model. A new
         * instance of the named class will be instantiated at run time using the
         * {@linkplain #withApplicationClassLoader(ClassLoader) application class loader}
         * provided, or the {@linkplain Thread#getContextClassLoader()
         * context class loader} of the thread used to invoke
         * {@link #build() build}.
         *
         * Filters will be executed in the order they are added.
         *
         * @param filterName OASFilter implementation class name
         * @return this builder
         */
        public Builder addFilterName(String filterName) {
            Objects.requireNonNull(filterName);
            filters.put(filterName, null);
            return this;
        }

        /**
         * Build a new {@linkplain SmallRyeOpenAPI} instance based on the current state of this builder.
         *
         * @param <V> JSON value type
         * @param <A> JSON array type
         * @param <O> JSON object type
         * @param <AB> JSON array builder type
         * @param <OB> JSON object builder type
         * @return a new {@linkplain SmallRyeOpenAPI} instance
         */
        public <V, A extends V, O extends V, AB, OB> SmallRyeOpenAPI build() {
            ClassLoader appClassLoader = applicationClassLoader != null ? applicationClassLoader
                    : Thread.currentThread().getContextClassLoader();

            OpenApiConfig buildConfig = OpenApiConfig.fromConfig(Optional.ofNullable(this.config)
                    .orElseGet(() -> ConfigProvider.getConfig(appClassLoader)));
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
                        .map(file -> {
                            try (Reader reader = new InputStreamReader(file.getContent())) {
                                V dom = io.jsonIO().fromReader(reader, file.getFormat());
                                OpenAPI fileModel = modelIO.readValue(dom);
                                debugModel("static file", fileModel);
                                return fileModel;
                            } catch (IOException e) {
                                throw new OpenApiRuntimeException("IOException reading " + file.getFormat() + " static file",
                                        e);
                            }
                        })
                        .reduce(MergeUtil::merge)
                        .orElse(null);
            }

            InputStream customFile = customStaticFile.get();

            if (customFile != null) {
                try (Reader reader = new InputStreamReader(customFile)) {
                    V dom = io.jsonIO().fromReader(reader);
                    OpenAPI customStaticModel = modelIO.readValue(dom);
                    debugModel("static file", customStaticModel);
                    staticModel = MergeUtil.merge(customStaticModel, staticModel);
                } catch (IOException e) {
                    throw new OpenApiRuntimeException("IOException reading custom static file", e);
                }
            }

            if (enableAnnotationScan && !buildConfig.scanDisable()) {
                buildConfig.setAllowNakedPathParameter(enableUnannotatedPathParameters);
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
            debugMap("callbacks", source, getMap(model, Components::getCallbacks));
            debugMap("examples", source, getMap(model, Components::getExamples));
            debugMap("headers", source, getMap(model, Components::getHeaders));
            debugMap("links", source, getMap(model, Components::getLinks));
            debugMap("parameters", source, getMap(model, Components::getParameters));
            debugMap("request bodies", source, getMap(model, Components::getRequestBodies));
            debugMap("responses", source, getMap(model, Components::getResponses));
            debugMap("schemas", source, getMap(model, Components::getSchemas));
            debugMap("security schemes", source, getMap(model, Components::getSecuritySchemes));
            debugList("servers", source, Optional.ofNullable(model.getServers()));
            debugMap("path items", source, Optional.ofNullable(model.getPaths()).map(Paths::getPathItems));
            debugList("security", source, Optional.ofNullable(model.getSecurity()));
            debugList("tags", source, Optional.ofNullable(model.getTags()));
            debugMap("extensions", source, Optional.ofNullable(model.getExtensions()));
        }

        private Optional<Map<String, ?>> getMap(OpenAPI model, Function<Components, Map<String, ?>> extract) {
            return Optional.ofNullable(model.getComponents()).map(extract);
        }

        private void debugMap(String name, String source, Optional<Map<String, ?>> collection) {
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
