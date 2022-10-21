package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingDeleteRoute;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetRoute;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingPostRoute;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingPutRoute;

/**
 * Basic Vert.x annotation scanning
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class VertxAnnotationScannerTest extends VertxDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testBasicGetRouteDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingGetRoute.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(failOnDuplicateOperationIdsConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicRouteGetDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testBasicPostRouteDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingPostRoute.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(failOnDuplicateOperationIdsConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicRoutePostDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testBasicPutRouteDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingPutRoute.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(failOnDuplicateOperationIdsConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicRoutePutDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testBasicDeleteRouteDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingDeleteRoute.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(failOnDuplicateOperationIdsConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicRouteDeleteDefinitionScanning.json", result);
    }

}
