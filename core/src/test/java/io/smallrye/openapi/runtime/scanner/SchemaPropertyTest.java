package io.smallrye.openapi.runtime.scanner;

import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

class SchemaPropertyTest extends IndexScannerTestBase {

    @Test
    void testClassSchemaPropertyMergesWithFieldSchemas() throws Exception {
        Index index = indexOf(Reptile.class, Lizard.class, Snake.class, Turtle.class, LengthUnits.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-merge.json", result);

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

    @Schema(name = "LengthUnitsEnum")
    static enum LengthUnits {
        CM,
        MM,
        IN
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
        int length;
        @Schema(description = "The units of measure for length", defaultValue = "MM")
        LengthUnits lengthUnits;
    }

    @Schema(allOf = { Reptile.class, Turtle.class })
    static class Turtle extends Reptile {
        String shellPattern;
    }
}
