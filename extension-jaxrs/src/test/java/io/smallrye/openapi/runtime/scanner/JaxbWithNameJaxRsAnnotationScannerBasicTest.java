package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.JaxbWithNameGreeting;
import test.io.smallrye.openapi.runtime.scanner.resources.*;

/**
 * Basic tests mostly to compare with Spring
 *
 * @author Andrey Batalev (andrey.batalev@gmail.com)
 */
public class JaxbWithNameJaxRsAnnotationScannerBasicTest extends JaxRsDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world GET service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicJaxRsGetDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(JaxbWithNameGreetingGetResource.class, JaxbWithNameGreeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxbWithNameJaxRsGetDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world POST service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicJaxRsPostDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(JaxbWithNameGreetingPostResource.class, JaxbWithNameGreeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxbWithNameJaxRsPostDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world PUT service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicJaxRsPutDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(JaxbWithNameGreetingPutResource.class, JaxbWithNameGreeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxbWithNameJaxRsPutDefinitionScanning.json", result);
    }

    /**
     * This test a basic, no OpenApi annotations, hello world DELETE service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicJaxRsDeleteDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(JaxbWithNameGreetingDeleteResource.class, JaxbWithNameGreeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxbWithNameJaxRsDeleteDefinitionScanning.json", result);
    }
}
