package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;

public class GenericResource {

    @GET
    @Path(value = "/extension")
    @Produces(value = MediaType.TEXT_PLAIN)
    public String helloExtension() {
        return "hello extension";
    }

    @GET
    @Path(value = "/extension-alt")
    @Produces(value = MediaType.TEXT_PLAIN)
    @Operation(description = "alternate extension")
    public String helloExtensionAlt() {
        return "hello extension alternate";
    }
}
