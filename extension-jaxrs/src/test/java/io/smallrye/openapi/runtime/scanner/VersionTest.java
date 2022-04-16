package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

/**
 * Basic tests to check the version configuration
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class VersionTest extends JaxRsDataObjectScannerTestBase {

    private static final String VERSION_PROPERTY = "mp.openapi.extensions.smallrye.openapi";

    /**
     * This test a basic that set the version by providing a schema document
     * 
     * @throws java.io.IOException
     * @throws org.json.JSONException
     */
    @Test
    void testJavaxSettingViaProvidedSchema() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource.class, Greeting.class);
        testSettingViaProvidedSchema(i);
    }

    @Test
    void testJakartaSettingViaProvidedSchema() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.jakarta.GreetingGetResource.class, Greeting.class);
        testSettingViaProvidedSchema(i);
    }

    void testSettingViaProvidedSchema(Index i) throws IOException, JSONException {
        OpenAPI result = OpenApiProcessor.bootstrap(emptyConfig(), i, loadStaticFile());

        printToConsole(result);
        assertJsonEquals("resource.testVersionViaSchema.json", result);
    }

    @Test
    void testJavaxSettingViaConfig() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource.class, Greeting.class);
        testSettingViaConfig(i);
    }

    @Test
    void testJakartaSettingViaConfig() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.jakarta.GreetingGetResource.class, Greeting.class);
        testSettingViaConfig(i);
    }

    void testSettingViaConfig(Index i) throws IOException, JSONException {
        System.setProperty(VERSION_PROPERTY, "3.0.0");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);
            printToConsole(result);
            assertJsonEquals("resource.testVersionViaConfig.json", result);

        } finally {
            System.clearProperty(VERSION_PROPERTY);
        }
    }

    @Test
    void testJavaxSettingViaConfigWhenStaticPresent() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource.class, Greeting.class);
        testSettingViaConfigWhenStaticPresent(i);
    }

    @Test
    void testJakartaSettingViaConfigWhenStaticPresent() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.jakarta.GreetingGetResource.class, Greeting.class);
        testSettingViaConfigWhenStaticPresent(i);
    }

    void testSettingViaConfigWhenStaticPresent(Index i) throws IOException, JSONException {
        System.setProperty(VERSION_PROPERTY, "3.0.0");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i, loadStaticFile());

            printToConsole(result);
            assertJsonEquals("resource.testVersionViaConfig.json", result);

        } finally {
            System.clearProperty(VERSION_PROPERTY);
        }
    }

    private OpenApiStaticFile loadStaticFile() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream versionJson = classLoader.getResourceAsStream("io/smallrye/openapi/runtime/scanner/static/version.json");
        return new OpenApiStaticFile(versionJson, Format.JSON);
    }

}
