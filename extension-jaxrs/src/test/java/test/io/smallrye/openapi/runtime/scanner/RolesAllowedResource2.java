package test.io.smallrye.openapi.runtime.scanner;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path(value = "/v2")
@SuppressWarnings(value = "unused")
public class RolesAllowedResource2 {

    @GET
    @Path(value = "secured")
    @Produces(value = "application/json")
    @RolesAllowed(value = { "admin", "users" })
    public Response getSecuredData(int id) {
        return null;
    }

}
