package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path(value = "/v1")
@RolesAllowed(value = "admin")
@SuppressWarnings(value = "unused")
public class RolesAllowedResource1 {

    @GET
    @Path(value = "secured")
    @Produces(value = "application/json")
    public Response getSecuredData(int id) {
        return null;
    }

    @GET
    @Path(value = "open")
    @Produces(value = "application/json")
    @PermitAll
    public Response getOpenData(int id) {
        return null;
    }

    @GET
    @Path(value = "locked")
    @Produces(value = "application/json")
    @DenyAll
    public Response getLockedData(int id) {
        return null;
    }

}
