package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedInput;
import org.json.JSONException;
import org.junit.Test;

public class RequestBodyScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    public void testResteasyMultipartInput() throws IOException, JSONException {
        test("params.resteasy-multipart-mixed.json",
                ResteasyMultipartInputTestResource.class);
    }

    @Test
    public void testResteasyMultipartInputList() throws IOException, JSONException {
        test("params.resteasy-multipart-mixed-array.json",
                ResteasyMultipartMixedListTestResource.class,
                RequestBodyWidget.class);
    }

    @Test
    public void testResteasyMultipartFormDataInput() throws IOException, JSONException {
        test("params.resteasy-multipart-form-data-input.json",
                ResteasyMultipartFormDataInputTestResource.class);
    }

    @Test
    public void testResteasyMultipartFormDataMap() throws IOException, JSONException {
        test("params.resteasy-multipart-form-data-map.json",
                ResteasyMultipartFormDataMapTestResource.class,
                RequestBodyWidget.class);
    }

    @Test
    public void testResteasyMultipartRelatedInput() throws IOException, JSONException {
        test("params.resteasy-multipart-related-input.json",
                ResteasyMultipartRelatedInputTestResource.class);
    }

    /***************** Test models and resources below. ***********************/

    public static class RequestBodyWidget {
        long id;
        String name;
    }

    @Path("multipart-mixed")
    static class ResteasyMultipartInputTestResource {
        @POST
        @Path("post")
        @Consumes("multipart/mixed")
        @SuppressWarnings("unused")
        public void post(MultipartInput input) {
        }
    }

    @Path("multipart-mixed-array")
    static class ResteasyMultipartMixedListTestResource {
        @POST
        @Path("post")
        @Consumes("multipart/mixed")
        @SuppressWarnings("unused")
        public void post(List<RequestBodyWidget> input) {
        }
    }

    @Path("multipart-form-data-input")
    static class ResteasyMultipartFormDataInputTestResource {
        @POST
        @Path("post")
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        @SuppressWarnings("unused")
        public void post(MultipartFormDataInput input) {
        }
    }

    @Path("multipart-form-data-map")
    static class ResteasyMultipartFormDataMapTestResource {
        @POST
        @Path("post")
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        @SuppressWarnings("unused")
        public void post(Map<String, RequestBodyWidget> input) {
        }
    }

    @Path("multipart-related-input")
    static class ResteasyMultipartRelatedInputTestResource {
        @POST
        @Path("post/{id}")
        @Consumes("multipart/related")
        @RequestBody(required = true)
        @SuppressWarnings("unused")
        public void post(@org.jboss.resteasy.annotations.jaxrs.PathParam("id") String id, MultipartRelatedInput input) {
        }
    }
}
