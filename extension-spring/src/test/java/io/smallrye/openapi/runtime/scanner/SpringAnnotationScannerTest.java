package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingDeleteController;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingDeleteControllerAlt;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetController;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetControllerAlt;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetControllerAlt2;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingPostController;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingPostControllerAlt;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingPutController;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingPutControllerAlt;

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
        Index i = indexOf(GreetingGetController.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

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
        Index i = indexOf(GreetingGetControllerAlt.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringGetDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * Here we use the alternative RequestMapping plus path rather than value
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicSpringDefinitionScanningAlt2() throws IOException, JSONException {
        Index i = indexOf(GreetingGetControllerAlt2.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringGetDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicPostSpringDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingPostController.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringPostDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicPostSpringDefinitionScanningAlt() throws IOException, JSONException {
        Index i = indexOf(GreetingPostControllerAlt.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringPostDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicPutSpringDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingPutController.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringPutDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicPutSpringDefinitionScanningAlt() throws IOException, JSONException {
        Index i = indexOf(GreetingPutControllerAlt.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringPutDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicDeleteSpringDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingDeleteController.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringDeleteDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicDeleteSpringDefinitionScanningAlt() throws IOException, JSONException {
        Index i = indexOf(GreetingDeleteControllerAlt.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringDeleteDefinitionScanning.json", result);
    }
}
