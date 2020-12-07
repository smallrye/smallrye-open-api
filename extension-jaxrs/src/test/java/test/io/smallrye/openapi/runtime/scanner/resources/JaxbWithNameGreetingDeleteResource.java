package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * JAX-RS.
 * Some basic tests, mostly to compare with the Spring implementation
 *
 * @author Andrey Batalev (andrey.batalev@gmail.com)
 */
@Path("/greeting")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
public class JaxbWithNameGreetingDeleteResource {

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
