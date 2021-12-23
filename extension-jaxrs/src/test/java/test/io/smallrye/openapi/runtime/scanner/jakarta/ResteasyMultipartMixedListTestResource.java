package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import test.io.smallrye.openapi.runtime.scanner.RequestBodyWidget;

@Path(value = "multipart-mixed-array")
public class ResteasyMultipartMixedListTestResource {

    @POST
    @Path(value = "post")
    @Consumes(value = "multipart/mixed")
    @SuppressWarnings(value = "unused")
    public void post(List<RequestBodyWidget> input) {
    }

}
