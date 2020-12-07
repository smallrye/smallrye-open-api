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

import test.io.smallrye.openapi.runtime.scanner.entities.JaxbWithNameGreeting;

/**
 * JAX-RS.
 * Some basic tests, mostly to compare with the Spring implementation
 *
 * @author Andrey Batalev (andrey.batalev@gmail.com)
 */
@Path("/greeting")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
public class JaxbWithNameGreetingGetResource {

    // 1) Basic path var test
    @GET
    @Path("/helloPathVariable/{name}")
    public JaxbWithNameGreeting helloPathVariable(@PathParam("name") String name) {
        return new JaxbWithNameGreeting("Hello " + name);
    }

    // 2) Basic path var that return a collection test
    @GET
    @Path("/hellosPathVariable/{name}")
    public List<JaxbWithNameGreeting> hellosPathVariable(@PathParam("name") String name) {
        return Arrays.asList(new JaxbWithNameGreeting("Hello " + name));
    }

    // 3) Basic path var with Optional test
    @GET
    @Path("/helloOptional/{name}")
    public Optional<JaxbWithNameGreeting> helloOptional(@PathParam("name") String name) {
        return Optional.of(new JaxbWithNameGreeting("Hello " + name));
    }

    // 4) Basic request param test
    @GET
    @Path("/helloRequestParam")
    public JaxbWithNameGreeting helloRequestParam(@QueryParam("name") String name) {
        return new JaxbWithNameGreeting("Hello " + name);
    }

    // 5) Response where you do not have a type.
    @GET
    @Path("/helloPathVariableWithResponse/{name}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/JaxbWithNameGreeting")))
    public Response helloPathVariableWithResponse(@PathParam("name") String name) {
        return Response.ok(new JaxbWithNameGreeting("Hello " + name)).build();
    }

    // 6) ResponseEntity with a type specified (No JaxRS comparison) (repeat of above as there is not wrapped type return
    @GET
    @Path("/helloPathVariableWithResponseTyped/{name}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/JaxbWithNameGreeting")))
    public Response helloPathVariableWithResponseTyped(@PathParam("name") String name) {
        return Response.ok(new JaxbWithNameGreeting("Hello " + name)).build();
    }

}
