package test.io.smallrye.openapi.runtime.scanner.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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
public class GreetingPostResource {

    // 1) Basic path var test
    @POST
    @Path("/greet")
    public Greeting greet(Greeting greeting) {
        return greeting;
    }

    // 2) Response where you do not have a type.
    @POST
    @Path("/greetWithResponse")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public Response greetWithResponse(Greeting greeting) {
        return Response.ok(greeting).build();
    }

    // 3) Response with a type specified (No JaxRS comparison) (repeat of above as there is not wrapped type return
    @POST
    @Path("/greetWithResponseTyped")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public Response greetWithResponseTyped(Greeting greeting) {
        return Response.ok(greeting).build();
    }

}
