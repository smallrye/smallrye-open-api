package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

class TagInheritanceTest extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        OpenAPI result = scan(classes);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testTagInherited() throws IOException, JSONException {
        @Tag(name = "qux", description = "ignored")
        abstract class RootResource {
        }

        @Tag(name = "foobar", description = "baz")
        abstract class BaseResource extends RootResource {
            @GET
            @Path("ping")
            @Produces(MediaType.TEXT_PLAIN)
            @Operation(summary = "Bla")
            public String ping() {
                return "ping";
            }
        }

        @Path("/foobar")
        class MyResource extends BaseResource {
            @GET
            @Path("pong")
            @Produces(MediaType.TEXT_PLAIN)
            @Operation(summary = "Bla")
            public String pong() {
                return "pong";
            }
        }

        test("resource.tags.inherited.json", RootResource.class, BaseResource.class, MyResource.class);
    }
}
