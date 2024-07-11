package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

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
        testSettingViaProvidedSchema(test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource.class,
                Greeting.class);
    }

    @Test
    void testJakartaSettingViaProvidedSchema() throws IOException, JSONException {
        testSettingViaProvidedSchema(test.io.smallrye.openapi.runtime.scanner.resources.jakarta.GreetingGetResource.class,
                Greeting.class);
    }

    void testSettingViaProvidedSchema(Class<?>... classes) throws IOException, JSONException {
        OpenAPI result = scan(config(Collections.emptyMap()), true, loadStaticFile("version-valid.json"), classes);
        assertJsonEquals("resource.testVersionViaSchema.json", result);
    }

    @Test
    void testJavaxSettingViaConfig() throws IOException, JSONException {
        testSettingViaConfig(test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource.class,
                Greeting.class);
    }

    @Test
    void testJakartaSettingViaConfig() throws IOException, JSONException {
        testSettingViaConfig(test.io.smallrye.openapi.runtime.scanner.resources.jakarta.GreetingGetResource.class,
                Greeting.class);
    }

    void testSettingViaConfig(Class<?>... classes) throws IOException, JSONException {
        System.setProperty(VERSION_PROPERTY, "3.1.2");

        try {
            OpenAPI result = scan(config(Collections.emptyMap()), true, null, classes);
            assertJsonEquals("resource.testVersionViaConfig.json", result);
        } finally {
            System.clearProperty(VERSION_PROPERTY);
        }
    }

    @Test
    void testJavaxSettingViaConfigWhenStaticPresent() throws IOException, JSONException {
        testSettingViaConfigWhenStaticPresent(
                test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource.class, Greeting.class);
    }

    @Test
    void testJakartaSettingViaConfigWhenStaticPresent() throws IOException, JSONException {
        testSettingViaConfigWhenStaticPresent(
                test.io.smallrye.openapi.runtime.scanner.resources.jakarta.GreetingGetResource.class, Greeting.class);
    }

    void testSettingViaConfigWhenStaticPresent(Class<?>... classes) throws IOException, JSONException {
        //The test will pass if this version matches the one in the file of expected JSON (resource.testVersionViaConfig.json) and the file read by loadStaticFile() (version.json) is overriden.
        System.setProperty(VERSION_PROPERTY, "3.1.2");

        try {
            OpenAPI result = scan(config(Collections.emptyMap()), true, loadStaticFile("version-broken.json"), classes);
            assertJsonEquals("resource.testVersionViaConfig.json", result);
        } finally {
            System.clearProperty(VERSION_PROPERTY);
        }
    }

    private InputStream loadStaticFile(String fileName) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream versionJson = classLoader.getResourceAsStream("io/smallrye/openapi/runtime/scanner/static/" + fileName);
        return versionJson;
    }

}
