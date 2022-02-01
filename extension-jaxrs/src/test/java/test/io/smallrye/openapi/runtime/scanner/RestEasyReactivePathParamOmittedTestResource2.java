package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
