package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

class SubresourceScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testJavaxResteasyMultipartInput() throws IOException, JSONException {
        test("resource.subresources-with-params.json",
                test.io.smallrye.openapi.runtime.scanner.javax.MainTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Sub1TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Sub2TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.RecursiveLocatorResource.class);
    }

    @Test
    void testJakartaResteasyMultipartInput() throws IOException, JSONException {
        test("resource.subresources-with-params.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.MainTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Sub1TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Sub2TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RecursiveLocatorResource.class);
    }

    @Tag(name = "root-a2-tag")
    @Retention(RetentionPolicy.RUNTIME)
    @interface RootA2Tag {
    }

    @Produces(MediaType.TEXT_PLAIN)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ProducesText {
    }

    @Test
    void testTagPlacementPriority() throws IOException, JSONException {
        @Tag(name = "subresource-a-tag")
        @ProducesText
        class SubresourceA {
            @GET
            @Path("/op1")
            @Tag(name = "subresource-a-op1-tag")
            public String op1() {
                return null;
            }

            @GET
            @Path("/op2")
            public String op2() {
                return null;
            }
        }

        @ProducesText
        class SubresourceB {
            @GET
            @Path("/op1")
            public String op1() {
                return null;
            }

            @GET
            @Path("/op2")
            @Tags()
            public String op2() {
                return null;
            }
        }

        @Path("/root")
        @Tag(name = "root-tag")
        class RootResource {
            @Path("a1")
            public SubresourceA getA1() {
                return null;
            }

            @Path("a2")
            @RootA2Tag
            public SubresourceA getA2() {
                return null;
            }

            @Path("b1")
            public SubresourceB getB1() {
                return null;
            }
        }

        test("resource.subresource-tag-placement-priority.json",
                RootResource.class, SubresourceA.class, SubresourceB.class, ProducesText.class, RootA2Tag.class);
    }
}
