package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetResource;
import test.io.smallrye.openapi.runtime.scanner.resources.GreetingOperationResource;

/**
 * Basic tests to check the operation Id autogeneration
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class OperationIdTest extends JaxRsDataObjectScannerTestBase {

    private static final String OPERATION_ID_PROPERTY = "mp.openapi.extensions.smallrye.operationIdStrategy";

    /**
     * This test a Method naming strategy
     * 
     * @throws java.io.IOException
     * @throws org.json.JSONException
     */
    @Test
    public void testMethodNaming() throws IOException, JSONException {
        System.setProperty(OPERATION_ID_PROPERTY, "METHOD");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingGetResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testOperationIdMethod.json", result);

        } finally {
            System.clearProperty(OPERATION_ID_PROPERTY);
        }
    }

    @Test
    public void testClassMethodNaming() throws IOException, JSONException {
        System.setProperty(OPERATION_ID_PROPERTY, "CLASS_METHOD");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingGetResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testOperationIdClassMethod.json", result);

        } finally {
            System.clearProperty(OPERATION_ID_PROPERTY);
        }
    }

    @Test
    public void testPackageClassMethodNaming() throws IOException, JSONException {
        System.setProperty(OPERATION_ID_PROPERTY, "PACKAGE_CLASS_METHOD");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingGetResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testOperationIdPackageClassMethod.json", result);

        } finally {
            System.clearProperty(OPERATION_ID_PROPERTY);
        }
    }

    @Test
    public void testMethodNamingWhenOperationIsSet() throws IOException, JSONException {
        System.setProperty(OPERATION_ID_PROPERTY, "METHOD");
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        try {
            Index i = indexOf(GreetingOperationResource.class, Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals("resource.testOperationIdMethodWithOperation.json", result);

        } finally {
            System.clearProperty(OPERATION_ID_PROPERTY);
        }
    }
}
