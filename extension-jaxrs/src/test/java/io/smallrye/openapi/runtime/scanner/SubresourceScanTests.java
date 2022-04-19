package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

class SubresourceScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<>()), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testJavaxResteasyMultipartInput() throws IOException, JSONException {
        test("resource.subresources-with-params.json",
                test.io.smallrye.openapi.runtime.scanner.javax.MainTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Sub1TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Sub2TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.RecursiveLocatorResource.class);
    }

    @Test
    void testJakartaResteasyMultipartInput() throws IOException, JSONException {
        test("resource.subresources-with-params.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.MainTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Sub1TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Sub2TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RecursiveLocatorResource.class);
    }
}
