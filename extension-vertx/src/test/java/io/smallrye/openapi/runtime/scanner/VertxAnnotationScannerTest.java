package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetRoute;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingPostRoute;

/**
 * Basic Vert.x annotation scanning
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class VertxAnnotationScannerTest extends VertxDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicGetRouteDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingGetRoute.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

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
    public void testBasicPostRouteDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingPostRoute.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

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
    //    @Test
    //    public void testBasicPutRouteDefinitionScanning() throws IOException, JSONException {
    //        Index i = indexOf(GreetingPutController.class, Greeting.class);
    //        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
    //
    //        OpenAPI result = scanner.scan();
    //
    //        printToConsole(result);
    //        assertJsonEquals("resource.testBasicRoutePutDefinitionScanning.json", result);
    //    }

    /**
     * This test a basic, no OpenApi annotations, hello world service
     * 
     * @throws IOException
     * @throws JSONException
     */
    //    @Test
    //    public void testBasicDeleteRouteDefinitionScanning() throws IOException, JSONException {
    //        Index i = indexOf(GreetingDeleteController.class, Greeting.class);
    //        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
    //
    //        OpenAPI result = scanner.scan();
    //
    //        printToConsole(result);
    //        assertJsonEquals("resource.testBasicRouteDeleteDefinitionScanning.json", result);
    //    }

}
