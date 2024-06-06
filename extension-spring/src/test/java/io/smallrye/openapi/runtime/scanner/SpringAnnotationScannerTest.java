package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

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
import test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingPostControllerWithServletContext;
import test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingPutControllerWithServletContext;

/**
 * Basic Spring annotation scanning
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class SpringAnnotationScannerTest extends SpringDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testBasicGetSpringDefinitionScanning() throws IOException, JSONException {
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
    void testBasicSpringDefinitionScanningAlt() throws IOException, JSONException {
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
    void testBasicSpringDefinitionScanningAlt2() throws IOException, JSONException {
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
    void testBasicPostSpringDefinitionScanning() throws IOException, JSONException {
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
    void testBasicPostSpringDefinitionScanningAlt() throws IOException, JSONException {
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
    void testBasicPostSpringDefinitionScanningWithServletContextJakarta() throws IOException, JSONException {
        Index i = indexOf(
                test.io.smallrye.openapi.runtime.scanner.resources.jakarta.GreetingPostControllerWithServletContext.class,
                Greeting.class);
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
    void testBasicPostSpringDefinitionScanningWithServletContextJavax() throws IOException, JSONException {
        Index i = indexOf(
                test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingPostControllerWithServletContext.class,
                Greeting.class);
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
    void testBasicPutSpringDefinitionScanning() throws IOException, JSONException {
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
    void testBasicPutSpringDefinitionScanningAlt() throws IOException, JSONException {
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
    void testBasicPutSpringDefinitionScanningWithServletContextJakarta() throws IOException, JSONException {
        Index i = indexOf(
                test.io.smallrye.openapi.runtime.scanner.resources.jakarta.GreetingPutControllerWithServletContext.class,
                Greeting.class);
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
    void testBasicPutSpringDefinitionScanningWithServletContextJavax() throws IOException, JSONException {
        Index i = indexOf(
                test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingPutControllerWithServletContext.class,
                Greeting.class);
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
    void testBasicDeleteSpringDefinitionScanning() throws IOException, JSONException {
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
    void testBasicDeleteSpringDefinitionScanningAlt() throws IOException, JSONException {
        Index i = indexOf(GreetingDeleteControllerAlt.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicSpringDeleteDefinitionScanning.json", result);
    }
}
