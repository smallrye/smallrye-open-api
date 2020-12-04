package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

/**
 * Endpoint with no produces or consumes
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Path("/greeting")
public class DefaultContentTypeResource {

    @GET
    @Path("/hello")
    public Greeting hello() {
        return new Greeting("Hello there");
    }

    @POST
    @Path("/hello")
    public Greeting hello(Greeting greeting) {
        return greeting;
    }

}
