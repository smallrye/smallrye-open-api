package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(value = "/movies")
public class RestEasyReactivePathParamOmittedTestResource2 {

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    public Widget get1(long id) {
        return null;
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path(value = "/{id2}")
    public Response get2(long id2) {
        return Response.ok().build();
    }
}
