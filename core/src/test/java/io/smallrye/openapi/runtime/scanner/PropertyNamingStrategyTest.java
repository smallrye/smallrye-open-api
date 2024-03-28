package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.constants.JsonbConstants;
import io.smallrye.openapi.runtime.OpenApiRuntimeException;

class PropertyNamingStrategyTest extends IndexScannerTestBase {

    @Test
    void testSnakeCase() throws Exception {
        OpenAPI result = scan(config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY,
                "com.fasterxml.jackson.databind.PropertyNamingStrategies$SnakeCaseStrategy"),
                NameStrategyBean1.class);
        assertJsonEquals("components.schemas.name-strategy-snake.json", result);
    }

    @Test
    void testJacksonNamingIgnoresConfig() throws Exception {
        OpenAPI result = scan(config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY,
                "com.fasterxml.jackson.databind.PropertyNamingStrategies$SnakeCaseStrategy"),
                NameStrategyBean2.class);
        assertJsonEquals("components.schemas.name-strategy-ignored.json", result);
    }

    @Test
    void testJacksonNamingOverridesConfig() throws Exception {
        OpenAPI result = scan(config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY,
                "com.fasterxml.jackson.databind.PropertyNamingStrategies$SnakeCaseStrategy"),
                NameStrategyKebab.class);
        assertJsonEquals("components.schemas.name-strategy-kebab.json", result);
    }

    @Test
    void testInvalidNamingStrategyClass() throws Exception {
        Config config = config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY,
                "com.fasterxml.jackson.databind.PropertyNamingStrategies$InvalidStrategy");
        assertThrows(OpenApiRuntimeException.class, () -> scan(config, NameStrategyKebab.class));
    }

    @Test
    void testNoValidTranslationMethods() throws Exception {
        Config config = config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY,
                NoValidTranslationMethods.class.getName());
        assertThrows(OpenApiRuntimeException.class, () -> scan(config, NameStrategyKebab.class));
    }

    @Test
    void testInvalidPropertyNameTranslationAttempt() throws Exception {
        Config config = config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY,
                TranslationThrowsException.class.getName());
        assertThrows(OpenApiRuntimeException.class, () -> scan(config, NameStrategyBean3.class));
    }

    @ParameterizedTest(name = "testJsonbConstantStrategy-{0}")
    @CsvSource({
            JsonbConstants.IDENTITY + ", simpleStringOne|anotherField|Y|z",
            JsonbConstants.LOWER_CASE_WITH_DASHES + ", simple-string-one|another-field|y|z",
            JsonbConstants.LOWER_CASE_WITH_UNDERSCORES + ", simple_string_one|another_field|y|z",
            JsonbConstants.UPPER_CAMEL_CASE + ", SimpleStringOne|AnotherField|Y|Z",
            JsonbConstants.UPPER_CAMEL_CASE_WITH_SPACES + ", Simple String One|Another Field|Y|Z",
            JsonbConstants.CASE_INSENSITIVE + ", simpleStringOne|anotherField|Y|z"
    })
    void testJsonbConstantStrategy(String strategy, String expectedNames) throws Exception {
        OpenAPI result = scan(config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY, strategy), NameStrategyBean3.class);
        Set<String> expectedNameSet = new TreeSet<>(Arrays.asList(expectedNames.split("\\|")));
        Map<String, org.eclipse.microprofile.openapi.models.media.Schema> schemas = result.getComponents().getSchemas();
        org.eclipse.microprofile.openapi.models.media.Schema schema = schemas.get(NameStrategyBean3.class.getSimpleName());
        assertEquals(expectedNameSet.size(), schema.getProperties().size());
        assertEquals(expectedNameSet, schema.getProperties().keySet());
    }

    @Schema
    static class NameStrategyBean1 {
        String simpleStringValue;
        @Schema(name = "another-string", description = "Customize name locally")
        String anotherStringValue;
        @Schema(maximum = "10", description = "Schema present, but name defaults to global config")
        int simpleIntegerValue;
    }

    @Schema
    @JsonNaming // Ignore configured strategy, no value
    static class NameStrategyBean2 {
        String simpleString1;
        Integer anotherField;
    }

    @Schema
    @JsonNaming(value = com.fasterxml.jackson.databind.PropertyNamingStrategies.KebabCaseStrategy.class)
    static class NameStrategyKebab {
        String simpleStringOne;
        Integer anotherField;
    }

    @Schema
    static class NameStrategyBean3 {
        String simpleStringOne;
        Integer anotherField;
        BigDecimal Y;
        double z;
    }

    public static class NoValidTranslationMethods {
        public NoValidTranslationMethods() {
        }

        public String translate() {
            return null;
        }

        public String translate(String v1, String v2) {
            return null;
        }

        public String translateValue(int v1) {
            return String.valueOf(v1);
        }

    }

    public static class TranslationThrowsException {
        public String translate(String value) {
            throw new IllegalArgumentException("dummy");
        }
    }
}
