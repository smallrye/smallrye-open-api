package test.io.smallrye.openapi.runtime.scanner.resources.javax;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;
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

    @GET
    @Path("/plain")
    public String justString() {
        return "Hello there";
    }

    @GET
    @Path("/plainOptional")
    public Optional<String> optionalString() {
        return Optional.of("Hello there");
    }

    @GET
    @Path("/plainUni")
    public Uni<String> uniString() {
        return Uni.createFrom().item("Hello there");
    }

    @GET
    @Path("/plainList")
    public List<String> listString() {
        return Arrays.asList(new String[] { "Hello there" });
    }

    @GET
    @Path("/goodbye")
    @Produces(MediaType.APPLICATION_XML)
    public Greeting byebye() {
        return new Greeting("Good Bye !");
    }

}
