package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

/**
 * Test the auto generation of Operation Id in the case that there is an operation annotation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Path("/greeting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GreetingOperationResource {

    @GET
    @Operation(description = "Here some description")
    @Path("/helloPathVariable/{name}")
    public Greeting helloPathVariable(@PathParam("name") String name) {
        return new Greeting("Hello " + name);
    }

    @GET
    @Operation(operationId = "myOwnId")
    @Path("/hellosPathVariable/{name}")
    public List<Greeting> hellosPathVariable(@PathParam("name") String name) {
        return Arrays.asList(new Greeting("Hello " + name));
    }

}
