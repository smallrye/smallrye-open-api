package test.io.smallrye.openapi.runtime.scanner;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
