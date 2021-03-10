package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

class ComponentOrderTest extends IndexScannerTestBase {

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/735
     */
    @Test
    void testComponentsKeysSorted() throws Exception {
        Index index = indexOf(Class.forName(getClass().getPackage().getName() + ".sorttest1.package-info"),
                Class.forName(getClass().getPackage().getName() + ".sorttest2.package-info"));
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        String[] expectedNames = { "123", "ABC", "DEF", "GHI", "KLM", "XYZ" };

        OpenAPI result = scanner.scan();
        printToConsole(result);

        assertArrayEquals(expectedNames, result.getComponents().getCallbacks().keySet().toArray());
        assertArrayEquals(expectedNames, result.getComponents().getExamples().keySet().toArray());
        assertArrayEquals(expectedNames, result.getComponents().getHeaders().keySet().toArray());
        assertArrayEquals(expectedNames, result.getComponents().getLinks().keySet().toArray());
        assertArrayEquals(expectedNames, result.getComponents().getParameters().keySet().toArray());
        assertArrayEquals(expectedNames, result.getComponents().getRequestBodies().keySet().toArray());
        assertArrayEquals(expectedNames, result.getComponents().getResponses().keySet().toArray());
        assertArrayEquals(expectedNames, result.getComponents().getSchemas().keySet().toArray());
        assertArrayEquals(expectedNames, result.getComponents().getSecuritySchemes().keySet().toArray());
    }

    @Test
    void testDefinitionTagOrderPreserved() throws Exception {
        Index index = indexOf(Class.forName(getClass().getPackage().getName() + ".sorttest1.package-info"),
                Class.forName(getClass().getPackage().getName() + ".sorttest2.package-info"));
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        String[] expectedNames = { "DEF", "XYZ", "ABC" };

        OpenAPI result = scanner.scan();
        printToConsole(result);

        assertArrayEquals(expectedNames, result.getTags().stream().map(Tag::getName).toArray());
    }
}
