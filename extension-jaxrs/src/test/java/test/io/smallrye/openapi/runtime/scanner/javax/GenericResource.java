package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class GenericResource {

    @GET
    @Path(value = "/extension")
    @Produces(value = MediaType.TEXT_PLAIN)
    public String helloExtension() {
        return "hello extension";
    }

}
