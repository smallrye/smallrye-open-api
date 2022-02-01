package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class GenericResource {

    @GET
    @Path(value = "/extension")
    @Produces(value = MediaType.TEXT_PLAIN)
    public String helloExtension() {
        return "hello extension";
    }

}
