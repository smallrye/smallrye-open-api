package test.io.smallrye.openapi.runtime.scanner;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path(value = "/v1")
@SuppressWarnings(value = "unused")
@DeclareRoles(value = { "admin", "users" })
public class RolesDeclaredResource {

    @GET
    @Path(value = "secured")
    @Produces(value = "application/json")
    @RolesAllowed(value = { "admin" })
    public Response getSecuredData(int id) {
        return null;
    }

}
