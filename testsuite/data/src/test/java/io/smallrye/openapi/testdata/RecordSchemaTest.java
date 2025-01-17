package io.smallrye.openapi.testdata;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.jandex.Index;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.SmallRyeOpenAPI;

/**
 * These tests can be moved to StandaloneSchemaScanTest once those tests are changed to compile with Java 17
 */
public class RecordSchemaTest {

    private static final Logger LOG = Logger.getLogger(RecordSchemaTest.class);

    @Schema
    record TestRecord(String a,
            @Schema(description = "b") String b) {
    }

    @Test
    void testRecordSchema() throws IOException, JSONException {

        Index index = Index.of(TestRecord.class);

        String result = SmallRyeOpenAPI.builder()
                .defaultRequiredProperties(false)
                .withIndex(index)
                .withConfig(config(emptyMap()))
                .build()
                .toJSON();

        LOG.debug(result);
        assertJsonEquals("components.schemas.record.json", result);
    }

    @Test
    void testRecordSchemaNoPrivateFields() throws IOException, JSONException {

        Index index = Index.of(TestRecord.class);

        String result = SmallRyeOpenAPI.builder()
                .defaultRequiredProperties(false)
                .withIndex(index)
                .withConfig(config(singletonMap(
                        SmallRyeOASConfig.SMALLRYE_PRIVATE_PROPERTIES_ENABLE,
                        "false")))
                .build()
                .toJSON();

        LOG.debug(result);
        assertJsonEquals("components.schemas.record.json", result);
    }

    private void assertJsonEquals(String expectedResource, String result) throws IOException, JSONException {
        String expected = loadResource(RecordSchemaTest.class.getResource(expectedResource));
        JSONAssert.assertEquals(expected, result, JSONCompareMode.STRICT);
    }

    public static String loadResource(URL testResource) throws IOException {
        final char[] buffer = new char[8192];
        final StringBuilder result = new StringBuilder();

        try (Reader reader = new InputStreamReader(testResource.openStream(), StandardCharsets.UTF_8)) {
            int count;
            while ((count = reader.read(buffer, 0, buffer.length)) > 0) {
                result.append(buffer, 0, count);
            }
        }

        return result.toString();
    }

    private static Config config(Map<String, String> properties) {
        return new SmallRyeConfigBuilder()
                .addDefaultSources()
                .withSources(new PropertiesConfigSource(properties, "unit-test", ConfigSource.DEFAULT_ORDINAL))
                .build();
    }

}
