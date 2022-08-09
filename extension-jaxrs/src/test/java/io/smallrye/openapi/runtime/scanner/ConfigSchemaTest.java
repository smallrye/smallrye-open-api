package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import jakarta.json.Json;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource;

/**
 * Basic tests to check the setting of schemas via the mp.openapi.schema.* config property
 * 
 * @author Scott Curtis (@literal <Scott.Curtis@ibm.com>)
 */
class ConfigSchemaTest extends JaxRsDataObjectScannerTestBase {

    private static final String VALID_SCHEMA_PROPERTY_KEY = "mp.openapi.schema.java.lang.String";
    private static final String INVALID_SCHEMA_PROPERTY_KEY = "mp.openapi.schema.java.lang.NonExistentClass";

    private static final String VALID_PROPERTY_VALUE = Json.createObjectBuilder()
            .add("name", "message")
            .add("type", "string")
            .add("description", "Mock custom String class defined with config")
            .build()
            .toString();

    private static final String INVALID_PROPERTY_VALUE_SCHEMA = Json.createObjectBuilder()
            .add("name", "message")
            .add("type", "string")
            .add("malformed-property", "This should not appear in output document")
            .build()
            .toString();

    private static final String INVALID_PROPERTY_VALUE_JSON = "{\"name\": \"message\","
            + "  \"type\": \"string\","
            + "  \"description\": \"Invali\"{d\"}";

    @Test
    void testValidSchemaDefinitionViaConfig() throws IOException, JSONException {
        System.setProperty(VALID_SCHEMA_PROPERTY_KEY, VALID_PROPERTY_VALUE);
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingGetResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testValidSchemaDefinitionViaConfig.json", result);

        } finally {
            System.clearProperty(VALID_SCHEMA_PROPERTY_KEY);
        }
    }

    // If the class in the key is non existent, schema is only rendered in components block
    @Test
    void testInvalidSchemaKeyDefinitionViaConfig() throws IOException, JSONException {
        System.setProperty(INVALID_SCHEMA_PROPERTY_KEY, VALID_PROPERTY_VALUE);
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingGetResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testInvalidSchemaKeyDefinitionViaConfig.json", result);

        } finally {
            System.clearProperty(INVALID_SCHEMA_PROPERTY_KEY);
        }
    }

    // Technically correct behaviour as malformed-property is not rendered in schema, but no feedback
    @Test
    void testValidSchemaKeyWithInvalidSchemaPropertyValueViaConfig() throws IOException, JSONException {
        System.setProperty(VALID_SCHEMA_PROPERTY_KEY, INVALID_PROPERTY_VALUE_SCHEMA);
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingGetResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testValidSchemaKeyWithInvalidSchemaPropertyValueViaConfig.json", result);

        } finally {
            System.clearProperty(VALID_SCHEMA_PROPERTY_KEY);
        }
    }

    // Technically correct behaviour as malformed schema is not rendered, but no feedback
    @Test
    void testValidSchemaKeyWithInvalidSchemaJSONValueViaConfig() throws IOException, JSONException {
        System.setProperty(VALID_SCHEMA_PROPERTY_KEY, INVALID_PROPERTY_VALUE_JSON);
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingGetResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testValidSchemaKeyWithInvalidSchemaJSONValueViaConfig.json", result);

        } finally {
            System.clearProperty(VALID_SCHEMA_PROPERTY_KEY);
        }
    }
}
