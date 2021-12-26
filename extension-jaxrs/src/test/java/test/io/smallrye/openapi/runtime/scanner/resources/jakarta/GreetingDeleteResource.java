package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * JAX-RS.
 * Some basic tests, mostly to compare with the Spring implementation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Path("/greeting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GreetingDeleteResource {

    // 1) Basic path var test
    @DELETE
    @Path("/greet/{id}")
    public void greet(@PathParam("id") String id) {

    }

    // 2) Response where you do not have a type.
    @DELETE
    @Path("/greetWithResponse/{id}")
    @APIResponse(responseCode = "204", description = "No Content")
    public Response greetWithResponse(@PathParam("id") String id) {
        return Response.noContent().build();
    }

    // 3) Response with a type specified (No JaxRS comparison) (repeat of above as there is not wrapped type return
    @DELETE
    @Path("/greetWithResponseTyped/{id}")
    @APIResponse(responseCode = "204", description = "No Content")
    public Response greetWithResponseTyped(@PathParam("id") String id) {
        return Response.noContent().build();
    }

}
