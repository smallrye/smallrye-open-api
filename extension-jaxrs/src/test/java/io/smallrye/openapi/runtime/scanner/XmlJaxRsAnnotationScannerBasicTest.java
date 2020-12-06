package io.smallrye.openapi.runtime.scanner;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.XmlGreetingDeleteResource;
import test.io.smallrye.openapi.runtime.scanner.resources.XmlGreetingGetResource;
import test.io.smallrye.openapi.runtime.scanner.resources.XmlGreetingPostResource;
import test.io.smallrye.openapi.runtime.scanner.resources.XmlGreetingPutResource;

import java.io.IOException;

/**
 * Basic tests mostly to compare with Spring
 *
 * @author Andrey Batalev (andrey.batalev@gmail.com)
 */
public class XmlJaxRsAnnotationScannerBasicTest extends JaxRsDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world GET service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicJaxRsGetDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(XmlGreetingGetResource.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicXmlJaxRsGetDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world POST service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicJaxRsPostDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(XmlGreetingPostResource.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicXmlJaxRsPostDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world PUT service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicJaxRsPutDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(XmlGreetingPutResource.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicXmlJaxRsPutDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world DELETE service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicJaxRsDeleteDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(XmlGreetingDeleteResource.class, Greeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicXmlJaxRsDeleteDefinitionScanning.json", result);
    }
}
