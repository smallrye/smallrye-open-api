package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
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
class VertxAnnotationScannerTest extends IndexScannerTestBase {

    void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        OpenAPI result = scan(classes);
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testBasicGetRouteDefinitionScanning() throws IOException, JSONException {
        test("resource.testBasicRouteGetDefinitionScanning.json", GreetingGetRoute.class, Greeting.class);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testBasicPostRouteDefinitionScanning() throws IOException, JSONException {
        test("resource.testBasicRoutePostDefinitionScanning.json", GreetingPostRoute.class, Greeting.class);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testBasicPutRouteDefinitionScanning() throws IOException, JSONException {
        test("resource.testBasicRoutePutDefinitionScanning.json", GreetingPutRoute.class, Greeting.class);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    void testBasicDeleteRouteDefinitionScanning() throws IOException, JSONException {
        test("resource.testBasicRouteDeleteDefinitionScanning.json", GreetingDeleteRoute.class, Greeting.class);
    }

}
