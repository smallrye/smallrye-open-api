package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Indexer;
import org.json.JSONException;
import org.junit.Test;

/**
 * Basic tests mostly to compare with Spring
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class JaxRsAnnotationScannerBasicTest extends JaxRsDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world GET service
     * 
     * @throws IOException
     * @throws JSONException
     */
    //@Test
    public void testBasicJaxRsGetDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/GreetingGetResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), indexer.complete());

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
    public void testBasicJaxRsPostDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/GreetingPostResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxRsPostDefinitionScanning.json", result);
    }

}
