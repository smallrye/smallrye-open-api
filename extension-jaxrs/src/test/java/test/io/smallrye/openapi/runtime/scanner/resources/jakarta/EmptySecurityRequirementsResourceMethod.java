package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

@Path("/public")
public class EmptySecurityRequirementsResourceMethod {

    @GET
    @SecurityRequirements
    public String getPublicResponse() {
        return "response value";
    }
}
