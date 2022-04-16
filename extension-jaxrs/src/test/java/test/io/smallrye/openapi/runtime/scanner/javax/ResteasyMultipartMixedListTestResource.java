package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

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
