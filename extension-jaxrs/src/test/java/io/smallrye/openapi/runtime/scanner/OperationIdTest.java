package io.smallrye.openapi.runtime.scanner;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.resources.jakarta.Salutation;
import test.io.smallrye.openapi.runtime.scanner.resources.jakarta.SalutationEnglish;
import test.io.smallrye.openapi.runtime.scanner.resources.jakarta.SalutationSpanish;

/**
 * Basic tests to check the operation Id autogeneration
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class OperationIdTest extends JaxRsDataObjectScannerTestBase {

    @ParameterizedTest
    @CsvSource({
            "METHOD, test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetResource, resource.testOperationIdMethod.json",
            "CLASS_METHOD, test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetResource, resource.testOperationIdClassMethod.json",
            "PACKAGE_CLASS_METHOD, test.io.smallrye.openapi.runtime.scanner.resources.GreetingGetResource, resource.testOperationIdPackageClassMethod.json",
            "METHOD, test.io.smallrye.openapi.runtime.scanner.resources.GreetingOperationResource, resource.testOperationIdMethodWithOperation.json"
    })
    void testOperationIdStrategies(String strategy, String resourceClass, String expectedResultResourceName)
            throws Exception {

        System.setProperty(OpenApiConstants.OPERATION_ID_STRAGEGY, strategy);
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);

        try {
            Index i = indexOf(Class.forName(resourceClass), Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals(expectedResultResourceName, result);
        } finally {
            System.clearProperty(OpenApiConstants.OPERATION_ID_STRAGEGY);
        }
    }

    @Test
    void testInheritedOperationIdsUtilizeConcreteClassName() throws Exception {
        try {
            OpenApiConfig config = dynamicConfig(OpenApiConstants.OPERATION_ID_STRAGEGY, "CLASS_METHOD");
            Index index = indexOf(Salutation.class, SalutationEnglish.class, SalutationSpanish.class);
            OpenAPI result = OpenApiProcessor.bootstrap(config, index);
            printToConsole(result);
            assertJsonEquals("resource.testOperationIdWithInheritance.json", result);
        } finally {
            System.clearProperty(OpenApiConstants.OPERATION_ID_STRAGEGY);
        }
    }
}
