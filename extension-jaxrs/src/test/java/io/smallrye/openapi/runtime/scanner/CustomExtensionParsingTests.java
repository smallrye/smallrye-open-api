package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callbacks;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
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
    void testDefaultExtensionParseThrowsJacksonNotFound() {
        Index index = IndexScannerTestBase.indexOf(ExtensionParsingTestResource.class);
        OpenApiConfig config = IndexScannerTestBase.emptyConfig();
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);
        NoClassDefFoundError err = assertThrows(NoClassDefFoundError.class, () -> scanner.scan());
        assertTrue(err.getMessage().contains("jackson"));
    }

    @Test
    void testCustomAnnotationScannerExtension() {
        Index index = IndexScannerTestBase.indexOf(ExtensionParsingTestResource.class);
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

    /* Test models and resources below. */

    @Path("/ext-custom")
    static class ExtensionParsingTestResource {
        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        @Callbacks({
                @Callback(name = "extendedCallback", callbackUrlExpression = "http://localhost:8080/resources/ext-callback", operations = @CallbackOperation(summary = "Get results", extensions = {
                        @Extension(name = "x-object", value = "{ \"key\":\"value\" }", parseValue = true),
                        @Extension(name = "x-object-unparsed", value = "{ \"key\":\"value\" }"),
                        @Extension(name = "x-array", value = "[ \"val1\",\"val2\" ]", parseValue = true),
                        @Extension(name = "x-booltrue", value = "true", parseValue = false)
                }, method = "get", responses = @APIResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = String.class)))))
        })
        public String get(String data) {
            return data;
        }
    }
}
