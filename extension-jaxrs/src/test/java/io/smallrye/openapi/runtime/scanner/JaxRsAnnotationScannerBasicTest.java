package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingDeleteResource;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetResource;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingPostResource;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingPutResource;

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
    @Test
    public void testBasicJaxRsGetDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingGetResource.class, Greeting.class);
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
    public void testBasicJaxRsPostDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingPostResource.class, Greeting.class);
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
    public void testBasicJaxRsPutDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingPutResource.class, Greeting.class);
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
    public void testBasicJaxRsDeleteDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(GreetingDeleteResource.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxRsDeleteDefinitionScanning.json", result);
    }
}
