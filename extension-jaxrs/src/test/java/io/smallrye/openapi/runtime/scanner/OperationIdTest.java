package io.smallrye.openapi.runtime.scanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOASConfig;
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

    @RegisterExtension
    public LogCapture logs = new LogCapture(ScannerLogging.class.getPackage().getName());

    @ParameterizedTest
    @CsvSource({
            "METHOD, test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource, resource.testOperationIdMethod.json",
            "CLASS_METHOD, test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource, resource.testOperationIdClassMethod.json",
            "PACKAGE_CLASS_METHOD, test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingGetResource, resource.testOperationIdPackageClassMethod.json",
            "METHOD, test.io.smallrye.openapi.runtime.scanner.resources.javax.GreetingOperationResource, resource.testOperationIdMethodWithOperation.json"
    })
    void testOperationIdStrategies(String strategy, String resourceClass, String expectedResultResourceName)
            throws Exception {

        System.setProperty(SmallRyeOASConfig.OPERATION_ID_STRAGEGY, strategy);
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfig.fromConfig(config);

        try {
            Index i = indexOf(Class.forName(resourceClass), Greeting.class);
            OpenAPI result = OpenApiProcessor.bootstrap(openApiConfig, i);

            printToConsole(result);
            assertJsonEquals(expectedResultResourceName, result);
        } finally {
            System.clearProperty(SmallRyeOASConfig.OPERATION_ID_STRAGEGY);
        }
    }

    @Test
    void testAppendNumberOnDuplicateOperationIds() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put(SmallRyeOASConfig.OPERATION_ID_STRAGEGY, "METHOD");
        config.put(SmallRyeOASConfig.DUPLICATE_OPERATION_ID_BEHAVIOR,
                OpenApiConfig.DuplicateOperationIdBehavior.APPEND_NUMBER.name());

        Index index = indexOf(AppendNumberTestResource.class);
        OpenAPI result = OpenApiProcessor.bootstrap(dynamicConfig(config), index);
        printToConsole(result);

        List<String> operationIds = result.getPaths().getPathItems().values().stream()
                .map(PathItem::getOperations)
                .flatMap(ops -> ops.values().stream())
                .map(Operation::getOperationId)
                .sorted()
                .collect(Collectors.toList());

        org.junit.jupiter.api.Assertions.assertEquals(List.of("create", "create_1"), operationIds);
    }

    @jakarta.ws.rs.Path("/append-number-test")
    static class AppendNumberTestResource {
        @jakarta.ws.rs.GET
        @jakarta.ws.rs.Path("/one")
        public String create() {
            return "one";
        }

        @jakarta.ws.rs.GET
        @jakarta.ws.rs.Path("/two")
        public String create(@jakarta.ws.rs.QueryParam("q") String param) {
            return "two";
        }
    }

    @Test
    void testInheritedOperationIdsUtilizeConcreteClassName() throws Exception {
        try {
            OpenApiConfig config = dynamicConfig(SmallRyeOASConfig.OPERATION_ID_STRAGEGY, "CLASS_METHOD");
            Index index = indexOf(Salutation.class, SalutationEnglish.class, SalutationSpanish.class);
            OpenAPI result = OpenApiProcessor.bootstrap(config, index);
            printToConsole(result);
            assertJsonEquals("resource.testOperationIdWithInheritance.json", result);
            logs.assertNoLogContaining("Duplicate operationId");
        } finally {
            System.clearProperty(SmallRyeOASConfig.OPERATION_ID_STRAGEGY);
        }
    }
}
