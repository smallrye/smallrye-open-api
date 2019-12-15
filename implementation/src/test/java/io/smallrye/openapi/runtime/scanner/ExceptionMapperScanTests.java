package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.testng.annotations.Test;

public class ExceptionMapperScanTests extends  IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    public void testExceptionMapper() throws IOException, JSONException {
        test("responses.exception-mapper-generation.json", TestResource.class, ExceptionHandler1.class, ExceptionHandler2.class);
    }



    @Path("/resources")
    static class TestResource {

        @GET
        public String getResource() throws NotFoundException {
            return "resource";
        }

        @POST
        public String createResource() throws WebApplicationException {
            return "OK";
        }
    }

    @Provider
    static class ExceptionHandler1 implements ExceptionMapper<WebApplicationException> {

        @Override
        @APIResponse(responseCode = "500", description = "Server error")
        public Response toResponse(WebApplicationException e) {
            return null;
        }
    }

    @Provider
    static class ExceptionHandler2 implements ExceptionMapper<NotFoundException> {

        @Override
        @APIResponse(responseCode = "404", description = "Not Found")
        public Response toResponse(NotFoundException e) {
            return null;
        }
    }


}
