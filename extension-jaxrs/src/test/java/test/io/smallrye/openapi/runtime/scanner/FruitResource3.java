package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(value = "/fruits")
public class FruitResource3 {

    @GET
    @Produces(value = MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

}
