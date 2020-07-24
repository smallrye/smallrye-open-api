package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetResource;

/**
 * Basic tests to check the version configuration
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class VersionTest extends JaxRsDataObjectScannerTestBase {

    private static final String VERSION_PROPERTY = "mp.openapi.extensions.openapi";

    /**
     * This test a basic that set the version by providing a schema document
     * 
     * @throws java.io.IOException
     * @throws org.json.JSONException
     */
    @Test
    public void testSettingViaProvidedSchema() throws IOException, JSONException {
        Index i = indexOf(GreetingGetResource.class, Greeting.class);
        OpenAPI result = OpenApiProcessor.bootstrap(emptyConfig(), i, loadStaticFile());

        printToConsole(result);
        assertJsonEquals("resource.testVersionViaSchema.json", result);
    }

    @Test
    public void testSettingViaConfig() throws IOException, JSONException {
        System.setProperty(VERSION_PROPERTY, "3.0.0");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingGetResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testVersionViaConfig.json", result);

        } finally {
            System.clearProperty(VERSION_PROPERTY);
        }
    }

    @Test
    public void testSettingViaConfigWhenStaticPresent() throws IOException, JSONException {
        System.setProperty(VERSION_PROPERTY, "3.0.0");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingGetResource.class, Greeting.class);
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
