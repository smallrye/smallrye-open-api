package io.smallrye.openapi.runtime.scanner;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.OpenAPIImpl;
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

    private static final Logger LOG = Logger.getLogger(OpenApiAnnotationScanner.class);

    private final AnnotationScannerContext annotationScannerContext;
    private final AnnotationScannerFactory annotationScannerFactory = new AnnotationScannerFactory();

    /**
     * Constructor.
     * 
     * @param config OpenApiConfig instance
     * @param index IndexView of deployment
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, IndexView index) {
        this(config, index, Collections.singletonList(new AnnotationScannerExtension() {
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
        FilteredIndexView filteredIndexView;

        if (index instanceof FilteredIndexView) {
            filteredIndexView = FilteredIndexView.class.cast(index);
        } else {
            filteredIndexView = new FilteredIndexView(index, config);
        }

        this.annotationScannerContext = new AnnotationScannerContext(filteredIndexView, extensions, config);
    }

    /**
     * Scan the deployment for relevant annotations. Returns an OpenAPI data model that was
     * built from those found annotations.
     * 
     * @return OpenAPI generated from scanning annotations
     */
    public OpenAPI scan() {
        try {
            // First scan the MicroProfile OpenAPI Annotations. Maybe later we can load this with SPI as well, and allow other Annotation sets.
            OpenAPI oai = scanMicroProfileOpenApiAnnotations();

            // Now load all entry points with SPI and scan those
            List<OpenAPI> scans = new LinkedList<>();
            List<AnnotationScanner> annotationScanners = annotationScannerFactory.getAnnotationScanners();
            for (AnnotationScanner annotationScanner : annotationScanners) {
                LOG.debug("Scanning deployment " + annotationScanner.getName() + " Annotations.");
                CurrentScannerInfo.register(annotationScanner);
                scans.add(annotationScanner.scan(annotationScannerContext, oai));
            }

            OpenAPI merged = MergeUtil.merge(scans);

            return merged;
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            throw new RuntimeException(npe);
        }
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
        LOG.debug("Scanning deployment for OpenAPI Annotations.");
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
            return (type) -> {
            };
        } else {
            try {
                return (CustomSchemaRegistry) Class.forName(config.customSchemaRegistryClass(), true, getContextClassLoader())
                        .newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                throw new RuntimeException("Failed to create instance of custom schema registry: "
                        + config.customSchemaRegistryClass(), ex);
            }
        }
    }

    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return AccessController
                .doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
    }
}
