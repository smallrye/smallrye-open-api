package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
