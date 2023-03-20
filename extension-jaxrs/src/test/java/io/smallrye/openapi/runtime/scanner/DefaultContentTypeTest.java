package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

/**
 * Basic tests to check the configuration of the default content type.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class DefaultContentTypeTest extends JaxRsDataObjectScannerTestBase {
    /**
     * This test the normal (no config) case
     *
     * @throws java.io.IOException
     * @throws org.json.JSONException
     */
    @Test
    void testJavaxVanilla() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.javax.DefaultContentTypeResource.class,
                Greeting.class);
        testVanilla(i);
    }

    @Test
    void testJakartaVanilla() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.jakarta.DefaultContentTypeResource.class,
                Greeting.class);
        testVanilla(i);
    }

    void testVanilla(Index i) throws IOException, JSONException {
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfig.fromConfig(config);
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
    void testJavaxConfigured() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.javax.DefaultContentTypeResource.class,
                Greeting.class);
        testConfigured(i);
    }

    @Test
    void testJakartaConfigured() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.jakarta.DefaultContentTypeResource.class,
                Greeting.class);
        testConfigured(i);
    }

    void testConfigured(Index i) throws IOException, JSONException {
        System.setProperty(OpenApiConstants.DEFAULT_CONSUMES, "application/json");
        System.setProperty(OpenApiConstants.DEFAULT_PRODUCES, "application/json");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfig.fromConfig(config);

        try {

            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testDefaultContentTypeConfigured.json", result);
        } finally {
            System.clearProperty(OpenApiConstants.DEFAULT_CONSUMES);
            System.clearProperty(OpenApiConstants.DEFAULT_PRODUCES);
        }
    }

}