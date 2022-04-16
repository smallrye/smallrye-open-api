package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * Basic tests mostly to compare with Spring
 *
 * @author Andrey Batalev (andrey.batalev@gmail.com)
 */
class JaxbJaxRsAnnotationScannerBasicTest extends JaxRsDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world GET service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testJavaxBasicJaxRsGetDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/JaxbGreetingGetResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/javax/JaxbGreeting.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/javax/JaxbWithNameGreeting.class");

        testBasicJaxRsGetDefinitionScanning(indexer.complete());
    }

    @Test
    void testJakartaBasicJaxRsGetDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/JaxbGreetingGetResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/jakarta/JaxbGreeting.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/jakarta/JaxbWithNameGreeting.class");

        testBasicJaxRsGetDefinitionScanning(indexer.complete());
    }

    void testBasicJaxRsGetDefinitionScanning(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxbJaxRsGetDefinitionScanning.json", result);
    }
}
