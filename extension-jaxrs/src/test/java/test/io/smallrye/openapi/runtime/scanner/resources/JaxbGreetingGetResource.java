package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import test.io.smallrye.openapi.runtime.scanner.entities.JaxbGreeting;

/**
 * JAX-RS.
 * Some basic tests, mostly to compare with the Spring implementation
 *
 * @author Andrey Batalev (andrey.batalev@gmail.com)
 */
@Path("/greeting")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
public class JaxbGreetingGetResource {

    // 1) Basic path var test
    @GET
    @Path("/helloPathVariable/{name}")
    public JaxbGreeting helloPathVariable(@PathParam("name") String name) {
        return new JaxbGreeting("Hello " + name);
    }

    // 2) Basic path var that return a collection test
    @GET
    @Path("/hellosPathVariable/{name}")
    public List<JaxbGreeting> hellosPathVariable(@PathParam("name") String name) {
        return Arrays.asList(new JaxbGreeting("Hello " + name));
    }

    // 3) Basic path var with Optional test
    @GET
    @Path("/helloOptional/{name}")
    public Optional<JaxbGreeting> helloOptional(@PathParam("name") String name) {
        return Optional.of(new JaxbGreeting("Hello " + name));
    }

    // 4) Basic request param test
    @GET
    @Path("/helloRequestParam")
    public JaxbGreeting helloRequestParam(@QueryParam("name") String name) {
        return new JaxbGreeting("Hello " + name);
    }

    // 5) Response where you do not have a type.
    @GET
    @Path("/helloPathVariableWithResponse/{name}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/JaxbGreeting")))
    public Response helloPathVariableWithResponse(@PathParam("name") String name) {
        return Response.ok(new JaxbGreeting("Hello " + name)).build();
    }

    // 6) ResponseEntity with a type specified (No JaxRS comparison) (repeat of above as there is not wrapped type return
    @GET
    @Path("/helloPathVariableWithResponseTyped/{name}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/JaxbGreeting")))
    public Response helloPathVariableWithResponseTyped(@PathParam("name") String name) {
        return Response.ok(new JaxbGreeting("Hello " + name)).build();
    }

}
