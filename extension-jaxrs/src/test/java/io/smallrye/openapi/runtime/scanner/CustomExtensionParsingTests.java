package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.smallrye.openapi.api.OpenApiConfig;

/**
 * Special tests using a custom {@link AnnotationScannerExtension#parseExtension(String, String)}
 * implementation. The tests in this class will only run when system property `classpath.jackson.excluded`
 * is set to `true`. In that case, the Jackson dependencies should not be present on the class path.
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
@EnabledIfSystemProperty(named = "classpath.jackson.excluded", matches = "true")
class CustomExtensionParsingTests {

    @Test
    void testJavaxDefaultExtensionParseThrowsJacksonNotFound() {
        Index index = IndexScannerTestBase
                .indexOf(test.io.smallrye.openapi.runtime.scanner.javax.ExtensionParsingTestResource1.class);
        testDefaultExtensionParseThrowsJacksonNotFound(index);
    }

    @Test
    void testJakartaDefaultExtensionParseThrowsJacksonNotFound() {
        Index index = IndexScannerTestBase
                .indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.ExtensionParsingTestResource1.class);
        testDefaultExtensionParseThrowsJacksonNotFound(index);
    }

    void testDefaultExtensionParseThrowsJacksonNotFound(Index index) {
        OpenApiConfig config = IndexScannerTestBase.emptyConfig();
        NoClassDefFoundError err = assertThrows(NoClassDefFoundError.class, () -> new OpenApiAnnotationScanner(config, index));
        assertTrue(err.getMessage().contains("jackson"));
    }

    @Test
    void testJavaxCustomAnnotationScannerExtension() {
        Index index = IndexScannerTestBase
                .indexOf(test.io.smallrye.openapi.runtime.scanner.javax.ExtensionParsingTestResource1.class);
        testCustomAnnotationScannerExtension(index);
    }

    @Test
    void testJakartaCustomAnnotationScannerExtension() {
        Index index = IndexScannerTestBase
                .indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.ExtensionParsingTestResource1.class);
        testCustomAnnotationScannerExtension(index);
    }

    void testCustomAnnotationScannerExtension(Index index) {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(IndexScannerTestBase.emptyConfig(), index,
                Arrays.asList(new AnnotationScannerExtension() {
                    @Override
                    public Object parseExtension(String name, String value) {
                        /*
                         * "parsing" consists of creating a singleton map with the
                         * extension name as the key and the unparsed value as the value
                         */
                        return Collections.singletonMap(name, value);
                    }

                    @Override
                    public Object parseValue(String value) {
                        return value;
                    }

                    @Override
                    public Schema parseSchema(String jsonSchema) {
                        return OASFactory.createSchema();
                    }
                }));

        OpenAPI result = scanner.scan();
        org.eclipse.microprofile.openapi.models.callbacks.Callback cb;
        cb = result.getPaths().getPathItem("/ext-custom").getPOST().getCallbacks().get("extendedCallback");
        Map<String, Object> ext = cb.getPathItem("http://localhost:8080/resources/ext-callback").getGET().getExtensions();
        assertEquals(4, ext.size());
        assertEquals(Collections.singletonMap("x-object", "{ \"key\":\"value\" }"), ext.get("x-object"));
        assertEquals("{ \"key\":\"value\" }", ext.get("x-object-unparsed"));
        assertEquals(Collections.singletonMap("x-array", "[ \"val1\",\"val2\" ]"), ext.get("x-array"));
        assertEquals("true", ext.get("x-booltrue"));
    }
}
