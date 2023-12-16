package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@Path(value = "/v1")
@RolesAllowed(value = "admin")
@SuppressWarnings(value = "unused")
public class RolesAllowedResource1 {

    @GET
    @Path(value = "secured")
    @Produces(value = "application/json")
    @SecurityRequirement(name = "rolesScheme")
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
