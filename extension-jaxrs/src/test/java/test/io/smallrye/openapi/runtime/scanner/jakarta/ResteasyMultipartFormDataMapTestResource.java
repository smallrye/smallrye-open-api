package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import test.io.smallrye.openapi.runtime.scanner.RequestBodyWidget;

@Path(value = "multipart-form-data-map")
public class ResteasyMultipartFormDataMapTestResource {

    @POST
    @Path(value = "post")
    @Consumes(value = MediaType.MULTIPART_FORM_DATA)
    @SuppressWarnings(value = "unused")
    public void post(Map<String, RequestBodyWidget> input) {
    }

}
