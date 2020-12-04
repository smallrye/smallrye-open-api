package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.junit.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.DefaultContentTypeResource;

/**
 * Basic tests to check the configuration of the default content type.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DefaultContentTypeTest extends JaxRsDataObjectScannerTestBase {
    private static final Logger LOG = Logger.getLogger(DefaultContentTypeTest.class);

    /**
     * This test the normal (no config) case
     * 
     * @throws java.io.IOException
     * @throws org.json.JSONException
     */
    @Test
    public void testVanilla() throws IOException, JSONException {
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);

        Index i = indexOf(DefaultContentTypeResource.class, Greeting.class);
        OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

        printToConsole(result);
        assertJsonEquals("resource.testDefaultContentTypeVanilla.json", result);

    }

    /**
     * This test the normal (no config) case
     * 
     * @throws java.io.IOException
     * @throws org.json.JSONException
     */
    @Test
    public void testConfigured() throws IOException, JSONException {
        System.setProperty(OpenApiConstants.DEFAULT_CONSUMES, "application/json");
        System.setProperty(OpenApiConstants.DEFAULT_PRODUCES, "application/json");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);

        try {
            Index i = indexOf(DefaultContentTypeResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testDefaultContentTypeConfigured.json", result);
        } finally {
            System.clearProperty(OpenApiConstants.DEFAULT_CONSUMES);
            System.clearProperty(OpenApiConstants.DEFAULT_PRODUCES);
        }
    }

}
