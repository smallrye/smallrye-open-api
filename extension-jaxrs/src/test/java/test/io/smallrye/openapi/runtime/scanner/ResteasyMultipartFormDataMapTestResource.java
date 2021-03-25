package test.io.smallrye.openapi.runtime.scanner;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path(value = "multipart-form-data-map")
public class ResteasyMultipartFormDataMapTestResource {

    @POST
    @Path(value = "post")
    @Consumes(value = MediaType.MULTIPART_FORM_DATA)
    @SuppressWarnings(value = "unused")
    public void post(Map<String, RequestBodyWidget> input) {
    }

}
