package io.smallrye.openapi.runtime.scanner;

import java.util.HashMap;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.Test;

class SmallRyeOpenApiPathAnnotationScannerTest extends JaxRsDataObjectScannerTestBase {

    @Test
    void testExampleResource() throws Exception {
        // given
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/smallrye/ExampleResource.class");
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, Object>()),
                indexer.complete());

        // when
        OpenAPI result = scanner.scan();

        // then
        printToConsole(result);
        assertJsonEquals("resource.smallrye-openapi-path.example.json", result);
    }
}
