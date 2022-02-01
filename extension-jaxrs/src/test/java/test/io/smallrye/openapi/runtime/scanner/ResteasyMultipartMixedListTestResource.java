package test.io.smallrye.openapi.runtime.scanner;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path(value = "multipart-mixed-array")
public class ResteasyMultipartMixedListTestResource {

    @POST
    @Path(value = "post")
    @Consumes(value = "multipart/mixed")
    @SuppressWarnings(value = "unused")
    public void post(List<RequestBodyWidget> input) {
    }

}
