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
    public void testBasicSpringDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/GreetingController.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Greeting.class");
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringDefinitionScanning.json", result);
    }

    @Test
    public void testUserControllerScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/PetController.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/UserController.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/StoreController.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Category.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Order.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Pet.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/Tag.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/User.class");
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringDefinitionScanning.json", result);
    }

}
