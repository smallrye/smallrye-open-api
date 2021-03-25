package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

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
