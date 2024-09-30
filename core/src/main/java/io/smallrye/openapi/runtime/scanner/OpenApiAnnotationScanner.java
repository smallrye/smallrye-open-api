package io.smallrye.openapi.runtime.scanner;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.OpenAPIDefinitionIO;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerFactory;

/**
 * Scans a deployment (using the archive and jandex annotation index) for OpenAPI annotations.
 * Also delegate to all other scanners.
 * These annotations, if found, are used to generate a valid
 * OpenAPI model. For reference, see:
 *
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#annotations
 *
 * @author eric.wittmann@gmail.com
 *
 * @deprecated use the {@link io.smallrye.openapi.api.SmallRyeOpenAPI
 *             SmallRyeOpenAPI} builder API instead. This class may be moved,
 *             have reduced visibility, or be removed in a future release.
 */
@Deprecated
public class OpenApiAnnotationScanner {

    private final AnnotationScannerContext annotationScannerContext;
    private final Supplier<Iterable<AnnotationScanner>> scannerSupplier;

    /**
     * Constructor.
     *
     * @param config OpenApiConfig instance
     * @param index IndexView of deployment
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, IndexView index) {
        this(config, ClassLoaderUtil.getDefaultClassLoader(), index, Collections.emptyList());
    }

    /**
     * Constructor.
     *
     * @param config OpenApiConfig instance
     * @param index IndexView of deployment
     * @param extensions A set of extensions to scanning
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, IndexView index, List<AnnotationScannerExtension> extensions) {
        this(config, ClassLoaderUtil.getDefaultClassLoader(), index, extensions);
    }

    /**
     * Constructor.
     *
     * @param config OpenApiConfig instance
     * @param index IndexView of deployment
     * @param extensions A set of extensions to scanning
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, IndexView index, List<AnnotationScannerExtension> extensions,
            boolean addDefaultExtension) {
        this(config, ClassLoaderUtil.getDefaultClassLoader(), index, extensions, addDefaultExtension);
    }

    /**
     * Constructor.
     *
     * @param config
     *        OpenApiConfig instance
     * @param loader
     *        ClassLoader to discover AnnotationScanner services (via
     *        ServiceLoader) as well as loading application classes
     * @param index
     *        IndexView of deployment
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, ClassLoader loader, IndexView index) {
        this(config, loader, index, Collections.emptyList());
    }

    /**
     * Constructor.
     *
     * @param config
     *        OpenApiConfig instance
     * @param loader
     *        ClassLoader to discover AnnotationScanner services (via
     *        ServiceLoader) as well as loading application classes
     * @param index
     *        IndexView of deployment
     * @param extensions
     *        A set of extensions to scanning
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, ClassLoader loader, IndexView index,
            List<AnnotationScannerExtension> extensions) {
        this(config, loader, index, new AnnotationScannerFactory(loader), extensions);
    }

    private OpenApiAnnotationScanner(OpenApiConfig config, ClassLoader loader, IndexView index,
            List<AnnotationScannerExtension> extensions,
            boolean addDefaultExtension) {
        this(config, loader, index, new AnnotationScannerFactory(loader), extensions, addDefaultExtension);
    }

    public OpenApiAnnotationScanner(OpenApiConfig config, ClassLoader loader, IndexView index,
            Supplier<Iterable<AnnotationScanner>> scannerSupplier) {
        this(config, loader, index, scannerSupplier, Collections.emptyList());
    }

    /**
     * Constructor.
     *
     * @param config
     *        OpenApiConfig instance
     * @param loader
     *        ClassLoader to load application classes
     * @param index
     *        IndexView of deployment
     * @param scannerSupplier
     *        supplier of AnnotationScanner instances to use to generate the
     *        OpenAPI model for the application
     * @param extensions
     *        A set of extensions to scanning
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, ClassLoader loader, IndexView index,
            Supplier<Iterable<AnnotationScanner>> scannerSupplier,
            List<AnnotationScannerExtension> extensions) {
        this(config, loader, index, scannerSupplier, extensions, false);
    }

    /**
     * Constructor.
     *
     * @param config
     *        OpenApiConfig instance
     * @param loader
     *        ClassLoader to load application classes
     * @param index
     *        IndexView of deployment
     * @param scannerSupplier
     *        supplier of AnnotationScanner instances to use to generate the
     *        OpenAPI model for the application
     * @param extensions
     *        A set of extensions to scanning
     * @param addDefaultExtension
     *        Whether the {@linkplain AnnotationScannerExtension.Default default extension} should always be added.
     */
    private OpenApiAnnotationScanner(OpenApiConfig config, ClassLoader loader, IndexView index,
            Supplier<Iterable<AnnotationScanner>> scannerSupplier,
            List<AnnotationScannerExtension> extensions,
            boolean addDefaultExtension) {
        FilteredIndexView filteredIndexView;

        if (index instanceof FilteredIndexView) {
            filteredIndexView = FilteredIndexView.class.cast(index);
        } else {
            filteredIndexView = new FilteredIndexView(index, config);
        }

        this.annotationScannerContext = new AnnotationScannerContext(filteredIndexView, loader, extensions, addDefaultExtension,
                config,
                null,
                OASFactory.createOpenAPI());
        this.scannerSupplier = scannerSupplier;
    }

    public OpenApiAnnotationScanner(AnnotationScannerContext context, Supplier<Iterable<AnnotationScanner>> scannerSupplier) {
        this.annotationScannerContext = context;
        this.scannerSupplier = scannerSupplier;
    }

    /**
     * Scan the deployment for relevant annotations. Returns an OpenAPI data model that was
     * built from those found annotations.
     *
     * @param filter Filter to only include certain scanners. Based on the scanner name. (JAX-RS, Spring, Vert.x)
     * @return OpenAPI generated from scanning annotations
     */
    public OpenAPI scan(String... filter) {
        Set<String> included = Optional.ofNullable(filter)
                .map(f -> Arrays.stream(f).distinct().collect(Collectors.toSet()))
                .orElseGet(Collections::emptySet);

        return scan(name -> included.isEmpty() || included.stream().anyMatch(name::equals));
    }

    /**
     * Scan the deployment for relevant annotations. Returns an OpenAPI data model that was
     * built from those found annotations.
     *
     * @param filter Predicate to only include certain scanners. Based on the scanner name. (JAX-RS, Spring, Vert.x)
     * @return OpenAPI generated from scanning annotations
     */
    public OpenAPI scan(Predicate<String> filter) {
        // First scan the MicroProfile OpenAPI Annotations. Maybe later we can load this with SPI as well, and allow other Annotation sets.
        OpenAPI openApi = scanMicroProfileOpenApiAnnotations();

        // Now load all entry points with SPI and scan those
        for (AnnotationScanner annotationScanner : getScanners(filter)) {
            ScannerLogging.logger.scanning(annotationScanner.getName());
            annotationScannerContext.setCurrentScanner(annotationScanner);
            openApi = annotationScanner.scan(annotationScannerContext, openApi);
        }

        sortTags(annotationScannerContext, openApi);
        sortMaps(openApi);

        return openApi;
    }

    private Iterable<AnnotationScanner> getScanners(Predicate<String> filter) {
        return StreamSupport.stream(scannerSupplier.get().spliterator(), false)
                .filter(scanner -> filter.test(scanner.getName()))
                .collect(Collectors.toList());
    }

    private OpenAPI scanMicroProfileOpenApiAnnotations() {

        // Initialize a new OAI document.  Even if nothing is found, this will be returned.
        OpenAPI openApi = this.annotationScannerContext.getOpenApi();
        openApi.setOpenapi(SmallRyeOASConfig.Defaults.VERSION);

        // Register custom schemas if available
        getCustomSchemaRegistry(annotationScannerContext.getConfig())
                .registerCustomSchemas(annotationScannerContext.getSchemaRegistry());

        // Find all OpenAPIDefinition annotations at the package level
        ScannerLogging.logger.scanning("OpenAPI");
        processPackageOpenAPIDefinitions(annotationScannerContext, openApi);

        processClassSchemas(annotationScannerContext);

        return openApi;
    }

    /**
     * Scans all <code>@OpenAPIDefinition</code> annotations present on <code>package-info</code>
     * classes known to the scanner's index.
     *
     * @param context scanning context
     * @param oai the current OpenAPI result
     * @return the created OpenAPI
     */
    private OpenAPI processPackageOpenAPIDefinitions(final AnnotationScannerContext context, OpenAPI oai) {
        List<AnnotationInstance> packageDefs = context.getIndex()
                .getAnnotations(Names.OPENAPI_DEFINITION)
                .stream()
                .filter(this::annotatedClasses)
                .filter(annotation -> annotation.target().asClass().name().withoutPackagePrefix().equals("package-info"))
                .collect(Collectors.toList());

        for (AnnotationInstance packageDef : packageDefs) {
            OpenAPI packageOai = context.io().openApiDefinitionIO().read(packageDef);
            oai = MergeUtil.merge(oai, packageOai);
        }
        return oai;
    }

    private CustomSchemaRegistry getCustomSchemaRegistry(final OpenApiConfig config) {
        if (config == null || config.customSchemaRegistryClass() == null) {
            // Provide default implementation that does nothing
            return type -> {
            };
        } else {
            try {
                return (CustomSchemaRegistry) Class
                        .forName(config.customSchemaRegistryClass(), true, annotationScannerContext.getClassLoader())
                        .getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw ScannerMessages.msg.failedCreateInstance(config.customSchemaRegistryClass(), ex);
            }

        }
    }

    private void processClassSchemas(final AnnotationScannerContext context) {
        context.setCurrentScanner(null);

        context.getIndex()
                .getAnnotations(SchemaConstant.DOTNAME_SCHEMA)
                .stream()
                .filter(this::annotatedClasses)
                .map(annotation -> Type.create(annotation.target().asClass().name(), Type.Kind.CLASS))
                .sorted(Comparator.comparing(Type::name)) // Process annotation classes in predictable order
                .forEach(type -> SchemaFactory.typeToSchema(context, type, null, context.getExtensions()));
    }

    private boolean annotatedClasses(AnnotationInstance annotation) {
        return Objects.equals(annotation.target().kind(), AnnotationTarget.Kind.CLASS);
    }

    /**
     * Sort the tags unless the application has defined the order in OpenAPIDefinition annotation(s)
     *
     * @param context scanning context
     * @param oai the openAPI model
     */
    private void sortTags(final AnnotationScannerContext context, OpenAPI oai) {
        List<Tag> tags = oai.getTags();

        // Sort the tags unless the application has defined the order in OpenAPIDefinition annotation(s)
        if (tags != null && !tags.isEmpty() && !tagsDefinedByOpenAPIDefinition(context)) {
            oai.setTags(tags.stream()
                    .sorted(Comparator.comparing(Tag::getName))
                    .collect(Collectors.toList()));
        }
    }

    private boolean tagsDefinedByOpenAPIDefinition(final AnnotationScannerContext context) {
        return context.getIndex().getAnnotations(Names.OPENAPI_DEFINITION)
                .stream()
                .map(definition -> definition.value(OpenAPIDefinitionIO.PROP_TAGS))
                .filter(Objects::nonNull)
                .map(AnnotationValue::asNestedArray)
                .anyMatch(definitionTags -> definitionTags.length > 0);
    }

    private void sortMaps(OpenAPI oai) {
        // Now that all paths have been created, sort them (we don't have a better way to organize them).
        sort(oai.getPaths(), Paths::getPathItems, Paths::setPathItems);

        final Components components = oai.getComponents();

        sort(components, Components::getCallbacks, Components::setCallbacks);
        sort(components, Components::getExamples, Components::setExamples);
        sort(components, Components::getHeaders, Components::setHeaders);
        sort(components, Components::getLinks, Components::setLinks);
        sort(components, Components::getParameters, Components::setParameters);
        sort(components, Components::getRequestBodies, Components::setRequestBodies);
        sort(components, Components::getResponses, Components::setResponses);
        sort(components, Components::getSchemas, Components::setSchemas);
        sort(components, Components::getSecuritySchemes, Components::setSecuritySchemes);
    }

    private <P, V> void sort(P parent, Function<P, Map<String, V>> source, BiConsumer<P, Map<String, V>> target) {
        if (parent == null) {
            return;
        }

        final Map<String, V> unsorted = source.apply(parent);

        if (unsorted == null || unsorted.isEmpty()) {
            return;
        }

        final Map<String, V> sorted = unsorted.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));

        target.accept(parent, sorted);
    }
}
