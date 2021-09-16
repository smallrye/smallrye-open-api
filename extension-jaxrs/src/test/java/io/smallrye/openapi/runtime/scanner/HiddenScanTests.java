package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class HiddenScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testJavaxHideComponent() throws IOException, JSONException {
        test("hidden.components.json", test.io.smallrye.openapi.runtime.scanner.FruitResource3.class,
                test.io.smallrye.openapi.runtime.scanner.HiddenFruit.class,
                test.io.smallrye.openapi.runtime.scanner.VisibleFruit.class);
    }

    @Test
    void testJakartaHideComponent() throws IOException, JSONException {
        test("hidden.components.json", test.io.smallrye.openapi.runtime.scanner.jakarta.FruitResource3.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.HiddenFruit.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.VisibleFruit.class);
    }
}
