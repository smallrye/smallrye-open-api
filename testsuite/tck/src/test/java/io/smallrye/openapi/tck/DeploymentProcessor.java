package io.smallrye.openapi.tck;

import static io.smallrye.openapi.runtime.OpenApiProcessor.getFilter;
import static io.smallrye.openapi.runtime.OpenApiProcessor.modelFromAnnotations;
import static io.smallrye.openapi.runtime.OpenApiProcessor.modelFromReader;
import static io.smallrye.openapi.runtime.io.Format.JSON;
import static io.smallrye.openapi.runtime.io.Format.YAML;
import static io.smallrye.openapi.runtime.io.OpenApiSerializer.serialize;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;

public class DeploymentProcessor implements ApplicationArchiveProcessor {
    private static Logger LOGGER = Logger.getLogger(DeploymentProcessor.class.getName());
    public static volatile ClassLoader classLoader;

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        if (archive instanceof WebArchive) {
            WebArchive war = (WebArchive) archive;
            war.addClass(OpenApiRegistration.class);
            war.addClass(OpenApiApplication.class);
            war.addClass(OpenApiEndpoint.class);
            war.addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

            // This sets the war name as the context root
            war.addAsWebInfResource(
                    new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<!DOCTYPE Configure PUBLIC \"-//Jetty//Configure//EN\" \"http://www.eclipse" +
                            ".org/jetty/configure.dtd\">\n" +
                            "<Configure class=\"org.eclipse.jetty.webapp.WebAppContext\">\n" +
                            "    <Set name=\"contextPath\">/" + war.getName() + "</Set>\n" +
                            "</Configure>\n"),
                    ArchivePaths.create("jetty-web.xml"));

            // Add the required dependencies
            String[] deps = {
                    "org.jboss.resteasy:resteasy-servlet-initializer",
                    "org.jboss.resteasy:resteasy-cdi",
                    "org.jboss.resteasy:resteasy-json-binding-provider",
                    "io.smallrye:smallrye-open-api-core",
                    "io.smallrye:smallrye-open-api-jaxrs"
            };
            File[] dependencies = Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile(new File("pom.xml"))
                    .resolve(deps)
                    .withoutTransitivity()
                    .asFile();
            war.addAsLibraries(dependencies);

            generateOpenAPI(war);

            LOGGER.log(Level.FINE, () -> war.toString(true));
        }
    }

    /**
     * Builds the OpenAPI file and copies it to the deployed application.
     */
    private static void generateOpenAPI(final WebArchive war) {
        OpenApiConfig openApiConfig = config(war);
        Index index = index(war, openApiConfig);
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();

        Optional<OpenAPI> annotationModel = ofNullable(modelFromAnnotations(openApiConfig, contextClassLoader, index));
        Optional<OpenAPI> readerModel = ofNullable(modelFromReader(openApiConfig, contextClassLoader));
        Optional<OpenAPI> staticFileModel = Stream.of(modelFromFile(openApiConfig, war, "/META-INF/openapi.json", JSON),
                modelFromFile(openApiConfig, war, "/META-INF/openapi.yaml", YAML),
                modelFromFile(openApiConfig, war, "/META-INF/openapi.yml", YAML))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(openAPI -> openAPI);

        OpenApiDocument document = OpenApiDocument.INSTANCE;
        document.reset();
        document.config(openApiConfig);
        annotationModel.ifPresent(document::modelFromAnnotations);
        readerModel.ifPresent(document::modelFromReader);
        staticFileModel.ifPresent(document::modelFromStaticFile);
        document.filter(getFilter(openApiConfig, contextClassLoader));
        document.initialize();
        OpenAPI openAPI = document.get();

        try {
            war.addAsManifestResource(new ByteArrayAsset(serialize(openAPI, JSON).getBytes(UTF_8)), "openapi.json");
            war.addAsManifestResource(new ByteArrayAsset(serialize(openAPI, YAML).getBytes(UTF_8)), "openapi.yaml");
        } catch (IOException e) {
            // Ignore
        }

        document.reset();
    }

    /**
     * Provides the Jandex index.
     */
    private static Index index(final WebArchive war, final OpenApiConfig config) {
        FilteredIndexView filteredIndexView = new FilteredIndexView(null, config);
        Indexer indexer = new Indexer();
        Collection<Node> classes = war.getContent(object -> object.get().endsWith(".class")).values();
        for (Node value : classes) {
            try {
                String resource = value.getPath().get().replaceAll("/WEB-INF/classes/", "");
                // We remove the OpenApinEndpoint so the /openapi is not generated
                if (resource.contains(OpenApiEndpoint.class.getSimpleName())) {
                    continue;
                }

                DotName dotName = DotName.createSimple(resource.replaceAll("/", ".").substring(0, resource.length() - 6));
                if (filteredIndexView.accepts(dotName)) {
                    indexer.index(DeploymentProcessor.class.getClassLoader().getResourceAsStream(resource));
                }
            } catch (IOException e) {
                // Ignore
            }
        }
        return indexer.complete();
    }

    /**
     * Creates the config from the microprofile-config.properties file in the application. The spec defines that the
     * config file may be present in two locations.
     */
    private static OpenApiConfig config(final WebArchive war) {
        Optional<Node> microprofileConfig = Stream.of(ofNullable(war.get("/META-INF/microprofile-config.properties")),
                ofNullable(war.get("/WEB-INF/classes/META-INF/microprofile-config.properties")))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(node -> node);

        if (!microprofileConfig.isPresent()) {
            return new OpenApiConfigImpl(ConfigProvider.getConfig());
        }

        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(microprofileConfig.get().getAsset().openStream(), UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SmallRyeConfig config = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .addDefaultInterceptors()
                .withSources(new PropertiesConfigSource(properties, "microprofile-config.properties"))
                .build();

        return new OpenApiConfigImpl(config);
    }

    private static Optional<OpenAPI> modelFromFile(OpenApiConfig openApiConfig, final WebArchive war, final String location,
            final Format format) {
        return ofNullable(war.get(location))
                .map(Node::getAsset)
                .map(asset -> new OpenApiStaticFile(asset.openStream(), format))
                .map(f -> OpenApiProcessor.modelFromStaticFile(openApiConfig, f));
    }
}
