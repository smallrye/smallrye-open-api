package io.smallrye.openapi.tck;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class DeploymentProcessor {
    private static Logger LOGGER = Logger.getLogger(DeploymentProcessor.class.getName());

    public void observeDeployment(@Observes final BeforeDeploy beforeDeploy) {
        DeploymentDescription deployment = beforeDeploy.getDeployment();
        Archive<?> testableArchive = deployment.getTestableArchive();
        if (testableArchive != null) {
            process(testableArchive);
        } else {
            process(deployment.getArchive());
        }
    }

    private void process(Archive<?> archive) {
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

            Index index = index(war);

            try (ByteArrayOutputStream indexOut = new ByteArrayOutputStream()) {
                new IndexWriter(indexOut).write(index);
                war.addAsManifestResource(new ByteArrayAsset(indexOut.toByteArray()), "jandex.idx");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            LOGGER.log(Level.FINE, () -> war.toString(true));
        }
    }

    /**
     * Provides the Jandex index.
     */
    private static Index index(final WebArchive war) {
        Instant start = Instant.now();
        Indexer indexer = new Indexer();

        war.getContent(object -> object.get().endsWith(".class")).values().forEach(clazz -> {
            try (InputStream resource = clazz.getAsset().openStream()) {
                indexer.index(resource);
            } catch (IOException e) {
                // Ignore
            }
        });

        Index index = indexer.complete();
        LOGGER.log(Level.INFO, () -> String.format("Indexed %s in %s", war.getName(), Duration.between(start, Instant.now())));
        return index;
    }
}
