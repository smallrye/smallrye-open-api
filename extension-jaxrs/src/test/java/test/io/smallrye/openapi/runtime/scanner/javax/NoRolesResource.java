package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path(value = "/v1")
@SuppressWarnings(value = "unused")
public class NoRolesResource {

    @GET
    @Path(value = "secured")
    @Produces(value = "application/json")
    @RolesAllowed(value = { "admin" })
    public Response getSecuredData(int id) {
        return null;
    }

}
