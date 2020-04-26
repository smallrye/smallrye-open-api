package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Indexer;
import org.json.JSONException;
import org.junit.Test;

/**
 * Basic Spring annotation scanning
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SpringAnnotationScannerTest extends SpringDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicGetSpringDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/GreetingGetController.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringGetDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * Here we use the alternative RequestMapping rather than GetMapping
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicSpringDefinitionScanningAlt() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/GreetingGetControllerAlt.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringGetDefinitionScanning.json", result);
    }
}
