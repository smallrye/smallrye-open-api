package io.smallrye.openapi.runtime.scanner;

import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.Test;

public class StandaloneSchemaScanTest extends IndexScannerTestBase {

    @Test
    public void testUnreferencedSchemasInComponents() throws Exception {
        Index index = indexOf(Cat.class, Dog.class, Class.forName(getClass().getPackage().getName() + ".package-info"));
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.unreferenced.json", result);
    }

    @Test
    public void testInheritanceAnyOf() throws Exception {
        Index index = indexOf(Reptile.class, Lizard.class, Snake.class, Turtle.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.inheritance.json", result);

    }

    /****************************************************************/

    static class Cat {
        public String name;
        @Schema(minimum = "1", maximum = "20")
        public int age;
        @Schema(nullable = true)
        public String color;
    }

    @Schema(name = "DogType")
    static class Dog {
        public String name;
        public int age;
        @Schema(required = true)
        public int volume;
    }

    @Schema(discriminatorProperty = "type", discriminatorMapping = {
            @DiscriminatorMapping(value = "lizard", schema = Lizard.class),
            @DiscriminatorMapping(value = "snake", schema = Snake.class),
            @DiscriminatorMapping(value = "turtle", schema = Turtle.class)
    })
    static abstract class Reptile {
        @Schema(required = true)
        private String type;
    }

    @Schema(allOf = { Reptile.class, Lizard.class })
    static class Lizard extends Reptile {
        String color;
    }

    @Schema(allOf = { Reptile.class, Snake.class })
    static class Snake extends Reptile {
        int length;
        String lengthUnits;
    }

    @Schema(allOf = { Reptile.class, Turtle.class })
    static class Turtle extends Reptile {
        String shellPattern;
    }
}
