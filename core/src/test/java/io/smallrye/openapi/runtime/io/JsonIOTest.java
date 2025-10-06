package io.smallrye.openapi.runtime.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.api.SmallRyeOASConfig;

abstract class JsonIOTest<V, A extends V, O extends V, AB, OB> {

    protected JsonIO<V, A, O, AB, OB> target;

    @ParameterizedTest
    @CsvSource({
            "{ \"key\": 3.1415 }, 3.1415",
            "{ \"key\": \"3.1415\" }, ",
            "{ \"key\": [ 3.1415 ] }, ",
    })
    void testGetJsonBigDecimal(String input, BigDecimal expected) {
        @SuppressWarnings("unchecked")
        O value = (O) target.fromString(input, Format.JSON);
        assertEquals(expected, target.getJsonBigDecimal(value, "key"));
    }

    @ParameterizedTest
    @CsvSource({
            "{ \"key\": 3.141592653589793238462643383279 }, 3.141592653589793238462643383279",
    })
    void testFromJsonBigDecimal(String input, String expected) {
        @SuppressWarnings("unchecked")
        O value = (O) target.fromString(input, Format.JSON);
        V jsonValue = target.getValue(value, "key");
        Object javaValue = target.fromJson(jsonValue);
        assertEquals(new BigDecimal(expected), javaValue);
    }

    @Test
    void testSimpleAliasExpansion() {
        String input = ""
                + "---\n"
                + "openapi: 3.1.0\n"
                + "info:\n"
                + "  title: &info_title Cool API Title\n"
                + "  description: *info_title"; // description points to title's value

        @SuppressWarnings("unchecked")
        O value = (O) target.fromString(input, Format.YAML);
        String title = target.getObject(value, "info")
                .map(info -> target.getString(info, "title"))
                .orElseThrow();
        String description = target.getObject(value, "info")
                .map(info -> target.getString(info, "description"))
                .orElseThrow();

        assertEquals("Cool API Title", title);
        assertEquals(title, description);
    }

    @Test
    @Tag("property:" + SmallRyeOASConfig.SMALLRYE_YAML_ALIAS_EXPANSION_ENABLE + "=false")
    void testSimpleAliasExpansionDisabled() {
        assumeTrue(getClass().equals(JacksonJsonIOTest.class), "Disabling alias expansion only supported for JacksonJsonIO");

        String input = ""
                + "---\n"
                + "openapi: 3.1.0\n"
                + "info:\n"
                + "  title: &info_title Cool API Title\n"
                + "  description: *info_title"; // description points to title's value

        @SuppressWarnings("unchecked")
        O value = (O) target.fromString(input, Format.YAML);
        String title = target.getObject(value, "info")
                .map(info -> target.getString(info, "title"))
                .orElseThrow();
        String description = target.getObject(value, "info")
                .map(info -> target.getString(info, "description"))
                .orElseThrow();

        assertEquals("Cool API Title", title);
        assertEquals("info_title", description);
    }

    @Test
    void testAliasedPathItemExpansion() throws IOException {
        O resource1;
        O resource2;

        try (InputStream source = getClass().getResourceAsStream("openapi-aliased-pathitem.yaml");
                Reader reader = new InputStreamReader(source)) {
            @SuppressWarnings("unchecked")
            O value = (O) target.fromReader(reader);
            resource1 = target.getObject(value, "paths")
                    .flatMap(paths -> target.getObject(paths, "/api/resource1"))
                    .orElseThrow();
            resource2 = target.getObject(value, "paths")
                    .flatMap(paths -> target.getObject(paths, "/api/resource2"))
                    .orElseThrow();
        }

        assertTrue(target.isObject(resource1));

        O r1get = target.getObject(resource1, "get").orElseThrow();
        O r2get = target.getObject(resource2, "get").orElseThrow();

        assertTrue(target.isObject(r1get));
        assertEquals(r1get, r2get);

        O r1post = target.getObject(resource1, "post").orElse(null);
        O r2post = target.getObject(resource2, "post").orElse(null);

        assertNull(r1post);
        assertTrue(target.isObject(r2post));
        assertEquals("putResource2", target.getString(r2post, "operationId"));

    }
}
