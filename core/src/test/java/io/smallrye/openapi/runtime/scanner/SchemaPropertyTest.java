package io.smallrye.openapi.runtime.scanner;

import java.util.Collections;
import java.util.Locale;

import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.SmallRyeOpenAPI;

class SchemaPropertyTest extends IndexScannerTestBase {

    @Test
    void testClassSchemaPropertyMergesWithFieldSchemas() throws Exception {
        Index index = indexOf(Reptile.class, Lizard.class, Snake.class, Turtle.class, LengthUnits.class, Speed.class);
        SmallRyeOpenAPI result1 = SmallRyeOpenAPI.builder()
                .withConfig(config(Collections.emptyMap()))
                .withIndex(index)
                .setDefaultRequiredProperties(false)
                .build();
        printToConsole(result1.model());
        assertJsonEquals("components.schemas.schemaproperty-merge.json", result1.model());
    }

    /****************************************************************/

    @Schema(discriminatorProperty = "type", discriminatorMapping = {
            @DiscriminatorMapping(value = "lizard", schema = Lizard.class),
            @DiscriminatorMapping(value = "snake", schema = Snake.class),
            @DiscriminatorMapping(value = "turtle", schema = Turtle.class)
    })
    static abstract class Reptile {
        @Schema(required = true)
        private String type;
    }

    // package private
    @Schema(name = "LengthUnitsEnum")
    static enum LengthUnits {
        CM,
        MM,
        IN;

        @com.fasterxml.jackson.annotation.JsonValue
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @Schema(name = "SpeedEnum")
    public static enum Speed {
        SLOW,
        SLOWER,
        SLOWEST;

        @com.fasterxml.jackson.annotation.JsonValue
        final String notMethod = "junk";

        @com.fasterxml.jackson.annotation.JsonValue
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }

        @com.fasterxml.jackson.annotation.JsonValue(false)
        public String annotationValueIsFalse() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @Schema(allOf = { Reptile.class, Lizard.class }, properties = {
            @SchemaProperty(name = "color", description = "The color of the lizard")
    })
    static class Lizard extends Reptile {
        @Schema(defaultValue = "GREEN", deprecated = false)
        String color;
    }

    @Schema(allOf = { Reptile.class, Snake.class }, properties = {
            @SchemaProperty(name = "lengthUnits", defaultValue = "CM")
    })
    static class Snake extends Reptile {
        @Schema(multipleOf = 0.1)
        double length;
        @Schema(description = "The units of measure for length", defaultValue = "MM")
        LengthUnits lengthUnits;
    }

    @Schema(allOf = { Reptile.class, Turtle.class })
    static class Turtle extends Reptile {
        String shellPattern;
        Speed speed;
    }
}
