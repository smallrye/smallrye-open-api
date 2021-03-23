package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/public")
public class EmptySecurityRequirementsResource {

    @GET
    @SecurityRequirements
    public String getPublicResponse() {
        return "response value";
    }
}
