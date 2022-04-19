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
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class JaxRsAnnotationScannerBasicTest extends JaxRsDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world GET service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testJavaxBasicJaxRsGetDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/GreetingGetResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");

        testBasicJaxRsGetDefinitionScanning(indexer.complete());
    }

    @Test
    void testJakartaBasicJaxRsGetDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/GreetingGetResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");

        testBasicJaxRsGetDefinitionScanning(indexer.complete());
    }

    void testBasicJaxRsGetDefinitionScanning(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxRsGetDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world POST service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testJavaxBasicJaxRsPostDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/GreetingPostResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");

        testBasicJaxRsPostDefinitionScanning(indexer.complete());
    }

    @Test
    void testJakartaBasicJaxRsPostDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/GreetingPostResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");

        testBasicJaxRsPostDefinitionScanning(indexer.complete());
    }

    void testBasicJaxRsPostDefinitionScanning(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxRsPostDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world PUT service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testJavaxBasicJaxRsPutDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/GreetingPutResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");

        testBasicJaxRsPutDefinitionScanning(indexer.complete());
    }

    @Test
    void testJakartaBasicJaxRsPutDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/GreetingPutResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");

        testBasicJaxRsPutDefinitionScanning(indexer.complete());
    }

    void testBasicJaxRsPutDefinitionScanning(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxRsPutDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world DELETE service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testJavaxBasicJaxRsDeleteDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/GreetingDeleteResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");

        testBasicJaxRsDeleteDefinitionScanning(indexer.complete());
    }

    @Test
    void testJakartaBasicJaxRsDeleteDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/GreetingDeleteResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");

        testBasicJaxRsDeleteDefinitionScanning(indexer.complete());
    }

    void testBasicJaxRsDeleteDefinitionScanning(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxRsDeleteDefinitionScanning.json", result);
    }
}
