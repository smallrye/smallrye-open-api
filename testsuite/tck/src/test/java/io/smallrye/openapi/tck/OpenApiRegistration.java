package io.smallrye.openapi.tck;

import static io.smallrye.openapi.runtime.io.Format.JSON;
import static io.smallrye.openapi.runtime.io.Format.YAML;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.resteasy.spi.ResteasyDeployment;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;

/**
 * Just a workaround to register the /openapi endpoint when an Application class is present in the TCK test. JAX-RS
 * skips registration of scanned resources if the Application class lists the resouces (which happens in some TCKs).
 *
 * This also creates the OpenAPI object and sets it in the context to be used by the OpenApiEndpoint.
 */
@WebServlet(urlPatterns = "/init", loadOnStartup = 1000)
public class OpenApiRegistration extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        final Map<String, ResteasyDeployment> deployments = (Map<String, ResteasyDeployment>) config.getServletContext()
                .getAttribute("resteasy.deployments");
        final ResteasyDeployment deployment = deployments.get("/");
        deployment.getRegistry().addPerRequestResource(OpenApiEndpoint.class);

        openApi(config.getServletContext());
    }

    private void openApi(final ServletContext servletContext) {
        try {
            Optional<OpenAPI> staticOpenApi = Stream
                    .of(readOpenApiFile(servletContext, "/META-INF/openapi.json", JSON),
                            readOpenApiFile(servletContext, "/META-INF/openapi.yaml", YAML),
                            readOpenApiFile(servletContext, "/META-INF/openapi.yml", YAML))
                    .filter(Optional::isPresent)
                    .findFirst()
                    .flatMap(file -> file);

            staticOpenApi.ifPresent(openAPI -> servletContext.setAttribute("OpenAPI", openAPI));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Optional<OpenAPI> readOpenApiFile(final ServletContext servletContext, final String location, final Format format)
            throws Exception {

        final URL resource = servletContext.getResource(location);
        if (resource == null) {
            return Optional.empty();
        }

        final OpenApiDocument document = OpenApiDocument.INSTANCE;
        try (OpenApiStaticFile staticFile = new OpenApiStaticFile(resource.openStream(), format)) {
            Config config = ConfigProvider.getConfig();
            OpenApiConfig openApiConfig = new OpenApiConfigImpl(config);
            document.reset();
            document.config(openApiConfig);
            document.filter(OpenApiProcessor.getFilter(openApiConfig, Thread.currentThread().getContextClassLoader()));
            document.modelFromStaticFile(
                    io.smallrye.openapi.runtime.OpenApiProcessor.modelFromStaticFile(openApiConfig, staticFile));
            document.initialize();
            return Optional.of(document.get());
        } finally {
            document.reset();
        }
    }
}
