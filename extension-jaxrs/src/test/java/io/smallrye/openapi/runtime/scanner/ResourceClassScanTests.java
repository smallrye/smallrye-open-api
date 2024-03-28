package io.smallrye.openapi.runtime.scanner;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOASConfig;

class ResourceClassScanTests extends JaxRsDataObjectScannerTestBase {

    @Test
    void testOverwriteJavaxPath() throws Exception {
        // given
        Class<?> resourceClass = test.io.smallrye.openapi.runtime.scanner.javax.FruitResource.class;
        Index index = indexOf(resourceClass,
                test.io.smallrye.openapi.runtime.scanner.Fruit.class,
                test.io.smallrye.openapi.runtime.scanner.Seed.class);
        Map<String, String> properties = new HashMap<>();
        properties.put(SmallRyeOASConfig.SCAN_RESOURCE_CLASS_PREFIX + resourceClass.getName(), "overwritten-resource-path");
        OpenApiConfig config = dynamicConfig(properties);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);

        // when
        OpenAPI result = scanner.scan();

        // then
        printToConsole(result);
        assertJsonEquals("resource.class.path.overwritten.json", result);
    }

    @Test
    void testOverwriteJakartaPath() throws Exception {
        // given
        Class<?> resourceClass = test.io.smallrye.openapi.runtime.scanner.jakarta.FruitResource.class;
        Index index = indexOf(resourceClass,
                test.io.smallrye.openapi.runtime.scanner.Fruit.class,
                test.io.smallrye.openapi.runtime.scanner.Seed.class);
        Map<String, String> properties = new HashMap<>();
        properties.put(SmallRyeOASConfig.SCAN_RESOURCE_CLASS_PREFIX + resourceClass.getName(), "overwritten-resource-path");
        OpenApiConfig config = dynamicConfig(properties);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);

        // when
        OpenAPI result = scanner.scan();

        // then
        printToConsole(result);
        assertJsonEquals("resource.class.path.overwritten.json", result);
    }

    @Test
    void testIncludeUnannotatedJavaxResource() throws Exception {
        // given
        Class<?> resourceClass = test.io.smallrye.openapi.runtime.scanner.javax.UnannotatedResource.class;
        Index index = indexOf(resourceClass);
        Map<String, String> properties = new HashMap<>();
        properties.put(SmallRyeOASConfig.SCAN_RESOURCE_CLASS_PREFIX + resourceClass.getName(), "overwritten-resource-path");
        OpenApiConfig config = dynamicConfig(properties);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);

        // when
        OpenAPI result = scanner.scan();

        // then
        printToConsole(result);
        assertJsonEquals("resource.class.path.unannotated.json", result);
    }

    @Test
    void testIncludeUnannotatedJakartaResource() throws Exception {
        // given
        Class<?> resourceClass = test.io.smallrye.openapi.runtime.scanner.jakarta.UnannotatedResource.class;
        Index index = indexOf(resourceClass);
        Map<String, String> properties = new HashMap<>();
        properties.put(SmallRyeOASConfig.SCAN_RESOURCE_CLASS_PREFIX + resourceClass.getName(), "overwritten-resource-path");
        OpenApiConfig config = dynamicConfig(properties);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);

        // when
        OpenAPI result = scanner.scan();

        // then
        printToConsole(result);
        assertJsonEquals("resource.class.path.unannotated.json", result);
    }
}
