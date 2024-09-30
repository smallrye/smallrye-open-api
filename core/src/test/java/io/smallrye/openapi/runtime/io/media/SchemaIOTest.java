package io.smallrye.openapi.runtime.io.media;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.SmallRyeOpenAPI;

class SchemaIOTest {

    @ParameterizedTest
    @ValueSource(strings = { "3.0.3", "3.1.0" })
    void testSchemaReferenceExpansion(String oasVersion) {
        var builder = SmallRyeOpenAPI.builder()
                .withCustomStaticFile(() -> new ByteArrayInputStream((""
                        + "{"
                        + "  \"openapi\": \"" + oasVersion + "\","
                        + "  \"components\": {"
                        + "    \"schemas\": {"
                        + "      \"s1\": { \"$ref\": \"s2\" },"
                        + "      \"s2\": { \"type\": \"string\" }"
                        + "    }"
                        + "  }"
                        + "}").getBytes()));

        System.setProperty(SmallRyeOASConfig.VERSION, oasVersion);
        OpenAPI result;

        try {
            result = builder.build().model();
        } finally {
            System.clearProperty(SmallRyeOASConfig.VERSION);
        }

        assertEquals(oasVersion, result.getOpenapi());
        assertEquals("#/components/schemas/s2", result.getComponents().getSchemas().get("s1").getRef());
    }

}
