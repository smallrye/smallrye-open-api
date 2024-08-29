package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class HiddenScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testJavaxHideComponent() throws IOException, JSONException {
        test("hidden.components.json", test.io.smallrye.openapi.runtime.scanner.javax.FruitResource3.class,
                test.io.smallrye.openapi.runtime.scanner.javax.HiddenFruit.class,
                test.io.smallrye.openapi.runtime.scanner.javax.VisibleFruit.class);
    }

    @Test
    void testJakartaHideComponent() throws IOException, JSONException {
        test("hidden.components.json", test.io.smallrye.openapi.runtime.scanner.jakarta.FruitResource3.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.HiddenFruit.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.VisibleFruit.class);
    }

    @jakarta.ws.rs.Path("/")
    interface FooResource {
        @jakarta.ws.rs.GET
        @jakarta.ws.rs.Path("/foo")
        @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.TEXT_PLAIN)
        @Operation(summary = "Gets the foo")
        @APIResponse(responseCode = "200", description = "The Foo")
        jakarta.ws.rs.core.Response getFoo();
    }

    @Test
    void testIgnoredAbstractResource() throws IOException, JSONException {
        @jakarta.ws.rs.Path("/")
        abstract class BaseBarResource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("/bar")
            @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.TEXT_PLAIN)
            @Operation(summary = "Gets the bar")
            @APIResponse(responseCode = "200", description = "The Bar")
            public jakarta.ws.rs.core.Response getBar() {
                return null;
            }
        }

        test("default.json", FooResource.class, BaseBarResource.class);
    }
}
