package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

class InterfaceInheritanceTest extends IndexScannerTestBase {

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #423.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/423
     *
     */
    @Test
    void testJavaxInterfaceInheritance() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.ImmutableEntity.class,
                test.io.smallrye.openapi.runtime.scanner.javax.MutableEntity.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Note.class,
                test.io.smallrye.openapi.runtime.scanner.javax.FruitResource2.class);
        testInterfaceInheritance(i);
    }

    @Test
    void testJakartaInterfaceInheritance() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.ImmutableEntity.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.MutableEntity.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Note.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.FruitResource2.class);
        testInterfaceInheritance(i);
    }

    void testInterfaceInheritance(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.interface-inheritance.json", result);
    }
}
