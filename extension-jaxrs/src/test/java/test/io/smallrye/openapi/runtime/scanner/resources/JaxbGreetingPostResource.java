package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
public class JaxbGreetingPostResource {

    // 1) Basic path var test
    @POST
    @Path("/greet")
    public JaxbGreeting greet(JaxbGreeting greeting) {
        return greeting;
    }

    // 2) Response where you do not have a type.
    @POST
    @Path("/greetWithResponse")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/JaxbGreeting")))
    public Response greetWithResponse(JaxbGreeting greeting) {
        return Response.ok(greeting).build();
    }

    // 3) Response with a type specified (No JaxRS comparison) (repeat of above as there is not wrapped type return
    @POST
    @Path("/greetWithResponseTyped")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/JaxbGreeting")))
    public Response greetWithResponseTyped(JaxbGreeting greeting) {
        return Response.ok(greeting).build();
    }

}
