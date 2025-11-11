package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
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
    void testInvalidNamingStrategyClass() {
        Config config = config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY,
                "com.fasterxml.jackson.databind.PropertyNamingStrategies$InvalidStrategy");
        assertThrows(OpenApiRuntimeException.class, () -> scan(config, NameStrategyKebab.class));
    }

    @Test
    void testNoValidTranslationMethods() {
        Config config = config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY,
                NoValidTranslationMethods.class.getName());
        assertThrows(OpenApiRuntimeException.class, () -> scan(config, NameStrategyKebab.class));
    }

    @Test
    void testInvalidPropertyNameTranslationAttempt() {
        Config config = config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY,
                TranslationThrowsException.class.getName());
        assertThrows(OpenApiRuntimeException.class, () -> scan(config, NameStrategyBean3.class));
    }

    @ParameterizedTest(name = "testJsonbConstantStrategy-{0}")
    @CsvSource({
            JsonbConstants.IDENTITY + ", simpleStringOne|anotherField|Y|z|SOMEValue",
            JsonbConstants.LOWER_CASE_WITH_DASHES + ", simple-string-one|another-field|y|z|some-value",
            JsonbConstants.LOWER_CASE_WITH_UNDERSCORES + ", simple_string_one|another_field|y|z|some_value",
            JsonbConstants.UPPER_CAMEL_CASE + ", SimpleStringOne|AnotherField|Y|Z|SOMEValue",
            JsonbConstants.UPPER_CAMEL_CASE_WITH_SPACES + ", Simple String One|Another Field|Y|Z|SOME Value",
            JsonbConstants.CASE_INSENSITIVE + ", simpleStringOne|anotherField|Y|z|SOMEValue"
    })
    void testJsonbConstantStrategy(String strategy, String expectedNames) {
        OpenAPI result = scan(config(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY, strategy), NameStrategyBean3.class);
        List<String> expectedNameList = Arrays.asList(expectedNames.split("\\|"));
        Collections.sort(expectedNameList, String::compareToIgnoreCase);

        Map<String, org.eclipse.microprofile.openapi.models.media.Schema> schemas = result.getComponents().getSchemas();
        org.eclipse.microprofile.openapi.models.media.Schema schema = schemas.get(NameStrategyBean3.class.getSimpleName());
        List<String> actualNameList = new ArrayList<>(schema.getProperties().keySet());
        Collections.sort(actualNameList, String::compareToIgnoreCase);

        assertEquals(expectedNameList, actualNameList);
    }

    @Test
    void testMethodNamePreserved() throws Exception {
        OpenAPI result = scan(NameStrategyBean4.class);
        assertJsonEquals("components.schemas.method-name-preserved.json", result);
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
        BigDecimal Y; // NOSONAR - naming intentional
        double z;
        String SOMEValue; // NOSONAR - naming intentional

        public String getSimpleStringOne() {
            return simpleStringOne;
        }

        public Integer getAnotherField() {
            return anotherField;
        }

        public BigDecimal getY() {
            return Y;
        }

        public double getZ() {
            return z;
        }

        public String getSOMEValue() {
            return SOMEValue;
        }
    }

    @Schema
    static class NameStrategyBean4 {
        @Schema(name = "TESTValue", title = "Test Value")
        Integer TestValue; // NOSONAR - naming intentional

        @Schema(name = "eValue", title = "e-Value")
        String EValue; // NOSONAR - naming intentional

        @Schema(description = "Property for TestValue")
        public Integer getTESTValue() {
            return TestValue;
        }

        @Schema(description = "Property for e-Value")
        public String geteValue() {
            return EValue;
        }

    }

    public static class NoValidTranslationMethods {
        public String translate() {
            return null;
        }

        /**
         * @param v1 unused, demonstrated unsuitable translate method signature
         * @param v2 unused, demonstrated unsuitable translate method signature
         */
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

    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(discriminatorProperty = "type", discriminatorMapping = {
            @DiscriminatorMapping(value = "bird", schema = Bird.class),
            @DiscriminatorMapping(value = "dog", schema = Dog.class),
    })
    interface Animal {
    }

    static class Bird implements Animal {
        String favoriteSong;
    }

    static class Dog implements Animal {
        String favoriteBall;
    }

    @Test
    void testJacksonNamingInherited() throws Exception {
        OpenAPI result = scan(Animal.class, Bird.class, Dog.class);
        assertJsonEquals("components.schemas.name-strategy-inherited.json", result);
    }
}
