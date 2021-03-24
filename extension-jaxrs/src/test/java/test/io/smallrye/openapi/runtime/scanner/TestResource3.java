package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
