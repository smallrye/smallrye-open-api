package io.smallrye.openapi.tck;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.SmallRyeOpenAPI;

/**
 * Just a workaround to register the /openapi endpoint when an Application class is present in the TCK test. JAX-RS
 * skips registration of scanned resources if the Application class lists the resources (which happens in some TCKs).
 *
 * This also creates the OpenAPI object and sets it in the context to be used by the OpenApiEndpoint.
 */
@WebServlet(urlPatterns = "/init", loadOnStartup = 1000)
public class OpenApiRegistration extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(OpenApiRegistration.class.getName());

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext servletContext = config.getServletContext();
        registerEndpoint(servletContext);
        generateOpenAPI(servletContext);
    }

    private void registerEndpoint(ServletContext servletContext) {
        Registry restEasyRoot = Optional.ofNullable(servletContext.getAttribute("resteasy.deployments"))
                .map(Map.class::cast)
                .map(deployments -> deployments.get("/"))
                .map(ResteasyDeployment.class::cast)
                .map(ResteasyDeployment::getRegistry)
                .orElseThrow(() -> new IllegalStateException("Missing expected 'resteasy.deployments' context attribute"));

        restEasyRoot.addPerRequestResource(OpenApiEndpoint.class);
    }

    private void generateOpenAPI(ServletContext servletContext) {
        Instant t0 = Instant.now();
        IndexReader indexReader = new IndexReader(servletContext.getResourceAsStream("/META-INF/jandex.idx"));
        Index index;

        try {
            index = indexReader.read();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LOGGER.log(Level.INFO,
                () -> String.format("Loaded index in %s", Duration.between(t0, Instant.now())));

        Instant t1 = Instant.now();

        SmallRyeOpenAPI result = SmallRyeOpenAPI.builder()
                .withConfig(config(servletContext))
                .withResourceLocator(path -> {
                    try {
                        return servletContext.getResource(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .withIndex(index)
                .withScannerFilter("JAX-RS"::equals)
                .build();

        LOGGER.log(Level.INFO,
                () -> String.format("Generated OpenAPI model in %s", Duration.between(t1, Instant.now())));

        Instant t2 = Instant.now();
        servletContext.setAttribute("OpenAPI.JSON", result.toJSON().getBytes(UTF_8));
        LOGGER.log(Level.INFO,
                () -> String.format("Serialized OpenAPI model to JSON in %s", Duration.between(t2, Instant.now())));

        Instant t3 = Instant.now();
        servletContext.setAttribute("OpenAPI.YAML", result.toYAML().getBytes(UTF_8));
        LOGGER.log(Level.INFO,
                () -> String.format("Serialized OpenAPI model to YAML in %s", Duration.between(t3, Instant.now())));
    }

    /**
     * Creates the config from the microprofile-config.properties file in the application. The spec defines that the
     * config file may be present in two locations.
     */
    private Config config(ServletContext servletContext) {
        InputStream nonstandardMpConfig = servletContext
                .getResourceAsStream("/META-INF/microprofile-config.properties");

        if (nonstandardMpConfig == null) {
            return ConfigProvider.getConfig();
        }

        Properties properties = new Properties();

        try (InputStreamReader reader = new InputStreamReader(nonstandardMpConfig, UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SmallRyeConfig config = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .addDefaultInterceptors()
                .withSources(new PropertiesConfigSource(properties, "microprofile-config.properties"))
                .build();

        return config;
    }

}
