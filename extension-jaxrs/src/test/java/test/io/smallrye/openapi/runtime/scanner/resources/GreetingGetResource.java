package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

/**
 * JAX-RS.
 * Some basic tests, mostly to compare with the Spring implementation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Path("/greeting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GreetingGetResource {

    // 1) Basic path var test
    @GET
    @Path("/helloPathVariable/{name}")
    public Greeting helloPathVariable(@PathParam("name") String name) {
        return new Greeting("Hello " + name);
    }

    // 2) Basic path var that return a collection test
    @GET
    @Path("/hellosPathVariable/{name}")
    public List<Greeting> hellosPathVariable(@PathParam("name") String name) {
        return Arrays.asList(new Greeting("Hello " + name));
    }

    // 3) Basic path var with Optional test
    @GET
    @Path("/helloOptional/{name}")
    public Optional<Greeting> helloOptional(@PathParam("name") String name) {
        return Optional.of(new Greeting("Hello " + name));
    }

    // 4) Basic request param test
    @GET
    @Path("/helloRequestParam")
    public Greeting helloRequestParam(@QueryParam("name") String name) {
        return new Greeting("Hello " + name);
    }

    // 5) Response where you do not have a type.
    @GET
    @Path("/helloPathVariableWithResponse/{name}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public Response helloPathVariableWithResponse(@PathParam("name") String name) {
        return Response.ok(new Greeting("Hello " + name)).build();
    }

    // 6) ResponseEntity with a type specified (No JaxRS comparison) (repeat of above as there is not wrapped type return
    @GET
    @Path("/helloPathVariableWithResponseTyped/{name}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public Response helloPathVariableWithResponseTyped(@PathParam("name") String name) {
        return Response.ok(new Greeting("Hello " + name)).build();
    }

}
