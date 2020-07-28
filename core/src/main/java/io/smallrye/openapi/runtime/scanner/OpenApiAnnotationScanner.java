package io.smallrye.openapi.runtime.scanner;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.IndexView;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.CurrentScannerInfo;
import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;
import io.smallrye.openapi.runtime.io.definition.DefinitionReader;
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
 */
public class OpenApiAnnotationScanner {

    private final AnnotationScannerContext annotationScannerContext;
    private final AnnotationScannerFactory annotationScannerFactory;

    /**
     * Constructor.
     * 
     * @param config OpenApiConfig instance
     * @param index IndexView of deployment
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, IndexView index) {
        this(config, ClassLoaderUtil.getDefaultClassLoader(), index,
                Collections.singletonList(new AnnotationScannerExtension() {
                }));
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
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, ClassLoader loader, IndexView index) {
        this(config, loader, index, Collections.singletonList(new AnnotationScannerExtension() {
        }));
    }

    /**
     * Constructor.
     * 
     * @param config OpenApiConfig instance
     * @param index IndexView of deployment
     * @param extensions A set of extensions to scanning
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, ClassLoader loader, IndexView index,
            List<AnnotationScannerExtension> extensions) {
        FilteredIndexView filteredIndexView;

        if (index instanceof FilteredIndexView) {
            filteredIndexView = FilteredIndexView.class.cast(index);
        } else {
            filteredIndexView = new FilteredIndexView(index, config);
        }

        this.annotationScannerContext = new AnnotationScannerContext(filteredIndexView, loader, extensions, config);
        this.annotationScannerFactory = new AnnotationScannerFactory(loader);
    }

    /**
     * Scan the deployment for relevant annotations. Returns an OpenAPI data model that was
     * built from those found annotations.
     * 
     * @return OpenAPI generated from scanning annotations
     */
    public OpenAPI scan() {
        // First scan the MicroProfile OpenAPI Annotations. Maybe later we can load this with SPI as well, and allow other Annotation sets.
        OpenAPI openApi = scanMicroProfileOpenApiAnnotations();

        // Now load all entry points with SPI and scan those
        List<AnnotationScanner> annotationScanners = annotationScannerFactory.getAnnotationScanners();
        for (AnnotationScanner annotationScanner : annotationScanners) {
            ScannerLogging.log.scanning(annotationScanner.getName());
            CurrentScannerInfo.register(annotationScanner);
            openApi = annotationScanner.scan(annotationScannerContext, openApi);
        }
        return openApi;
    }

    private OpenAPI scanMicroProfileOpenApiAnnotations() {

        // Initialize a new OAI document.  Even if nothing is found, this will be returned.
        OpenAPI openApi = new OpenAPIImpl();
        openApi.setOpenapi(OpenApiConstants.OPEN_API_VERSION);

        // Creating a new instance of a registry which will be set on the thread context.
        SchemaRegistry schemaRegistry = SchemaRegistry.newInstance(annotationScannerContext.getConfig(), openApi,
                annotationScannerContext.getIndex());

        // Register custom schemas if available
        getCustomSchemaRegistry(annotationScannerContext.getConfig()).registerCustomSchemas(schemaRegistry);

        // Find all OpenAPIDefinition annotations at the package level
        ScannerLogging.log.scanning("OpenAPI");
        processPackageOpenAPIDefinitions(annotationScannerContext, openApi);

        return openApi;
    }

    /**
     * Scans all <code>@OpenAPIDefinition</code> annotations present on <code>package-info</code>
     * classes known to the scanner's index.
     * 
     * @param oai the current OpenAPI result
     * @return the created OpenAPI
     */
    private OpenAPI processPackageOpenAPIDefinitions(final AnnotationScannerContext context, OpenAPI oai) {
        List<AnnotationInstance> packageDefs = context.getIndex()
                .getAnnotations(DefinitionConstant.DOTNAME_OPEN_API_DEFINITION)
                .stream()
                .filter(annotation -> annotation.target().kind() == AnnotationTarget.Kind.CLASS)
                .filter(annotation -> annotation.target().asClass().name().withoutPackagePrefix().equals("package-info"))
                .collect(Collectors.toList());

        for (AnnotationInstance packageDef : packageDefs) {
            OpenAPI packageOai = new OpenAPIImpl();
            DefinitionReader.processDefinition(context, packageOai, packageDef);
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
}
