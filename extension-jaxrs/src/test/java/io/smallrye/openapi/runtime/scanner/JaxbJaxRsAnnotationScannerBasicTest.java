package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.entities.JaxbGreeting;
import test.io.smallrye.openapi.runtime.scanner.entities.JaxbWithNameGreeting;
import test.io.smallrye.openapi.runtime.scanner.resources.*;

/**
 * Basic tests mostly to compare with Spring
 *
 * @author Andrey Batalev (andrey.batalev@gmail.com)
 */
public class JaxbJaxRsAnnotationScannerBasicTest extends JaxRsDataObjectScannerTestBase {

    /**
     * This test a basic, no OpenApi annotations, hello world GET service
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testBasicJaxRsGetDefinitionScanning() throws IOException, JSONException {
        Index i = indexOf(JaxbGreetingGetResource.class, Greeting.class, JaxbGreeting.class, JaxbWithNameGreeting.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testBasicJaxbJaxRsGetDefinitionScanning.json", result);
    }
}
