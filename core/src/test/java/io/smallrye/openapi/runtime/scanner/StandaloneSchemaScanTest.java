package io.smallrye.openapi.runtime.scanner;

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

}
