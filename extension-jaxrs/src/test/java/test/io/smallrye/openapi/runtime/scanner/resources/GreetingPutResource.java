package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
public class GreetingPutResource {

    // 1) Basic path var test
    @PUT
    @Path("/greet/{id}")
    public Greeting greet(Greeting greeting, @PathParam("id") String id) {
        return greeting;
    }

    // 2) Response where you do not have a type.
    @PUT
    @Path("/greetWithResponse/{id}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public Response greetWithResponse(Greeting greeting, @PathParam("id") String id) {
        return Response.ok(greeting).build();
    }

    // 3) Response with a type specified (No JaxRS comparison) (repeat of above as there is not wrapped type return
    @PUT
    @Path("/greetWithResponseTyped/{id}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public Response greetWithResponseTyped(Greeting greeting, @PathParam("id") String id) {
        return Response.ok(greeting).build();
    }

}
