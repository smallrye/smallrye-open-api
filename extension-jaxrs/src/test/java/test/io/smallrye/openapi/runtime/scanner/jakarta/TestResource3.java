package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import test.io.smallrye.openapi.runtime.scanner.Apple;

@Path(value = "/generic")
@Consumes(value = MediaType.APPLICATION_JSON)
@Produces(value = MediaType.APPLICATION_JSON)
public class TestResource3 extends BaseResource2<Apple, String> {

    @POST
    @Path(value = "save")
    public Apple update(Apple filter) {
        return null;
    }

}
