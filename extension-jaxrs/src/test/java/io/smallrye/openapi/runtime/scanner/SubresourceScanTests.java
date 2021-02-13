package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

public class SubresourceScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, Object>()), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    public void testResteasyMultipartInput() throws IOException, JSONException {
        test("resource.subresources-with-params.json",
                MainTestResource.class, Sub1TestResource.class, Sub2TestResource.class, RecursiveLocatorResource.class);
    }

    /***************** Test models and resources below. ***********************/

    @Path("/resource")
    @SuppressWarnings("unused")
    static class MainTestResource {
        public MainTestResource(@MatrixParam("r0m1") LocalDateTime m0) {
        }

        public void setSomethingElse(Long code) {
        }

        @MatrixParam("r0m0")
        public void setM0(LocalDateTime m0) {
        }

        @Path("/sub/unknown1")
        @Parameter(name = "u1q", in = ParameterIn.QUERY, style = ParameterStyle.SIMPLE, description = "Parameter to make a sub-resource locator look like a bean property param")
        public Object getUnknownResource1(Long code) {
            return null;
        }

        @Path("/sub/unknown2")
        public Object getUnknownResource2(
                @Parameter(name = "u2q", in = ParameterIn.QUERY, style = ParameterStyle.SIMPLE, description = "Parameter to make a sub-resource locator look like a bean property param") Long code) {
            return null;
        }

        @Path("/sub0")
        @GET
        @Parameter(name = "q4", description = "Q4 Query")
        public String getHello(@QueryParam("q4") String q4) {
            return "hello";
        }

        @Path("/sub/{id}")
        @Parameter(name = "id", description = "Resource Identifier")
        public Sub1TestResource<String> get(@PathParam("id") String id,
                @QueryParam("q1") String q1,
                @MatrixParam("m1") String m1,
                @MatrixParam("m2") int m2,
                @FormParam("f1") String f1) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    static class Sub1TestResource<T> {
        @QueryParam("q2")
        T q2;

        @GET
        public String get(@QueryParam("q3") String q3) {
            return null;
        }

        @PATCH
        @Consumes(MediaType.TEXT_PLAIN)
        public void update(String value) {
            return;
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        public void create(Map<String, CharSequence> attributes) {
            return;
        }

        @Path("/sub2")
        public Sub2TestResource<T> getSub2() {
            return new Sub2TestResource<T>();
        }
    }

    @SuppressWarnings("unused")
    static class Sub2TestResource<T> {
        @GET
        @Path("{subsubid}")
        public T getSub2(@PathParam("subsubid") String subsubid) {
            return null;
        }
    }

    @Path("/recursion")
    @SuppressWarnings("unused")
    static class RecursiveLocatorResource {
        @GET
        @Path("fetch")
        public String get() {
            return null;
        }

        @Path("alternate1")
        public RecursiveLocatorResource getLocator1() {
            return this;
        }

        @Path("alternate2")
        public RecursiveLocatorResource getLocator2() {
            return this;
        }
    }
}
