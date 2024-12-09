package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
