package test.io.smallrye.openapi.runtime.scanner.resources.javax;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

@Path("/public")
public class EmptySecurityRequirementsResourceMethod {

    @GET
    @SecurityRequirements
    public String getPublicResponse() {
        return "response value";
    }
}
