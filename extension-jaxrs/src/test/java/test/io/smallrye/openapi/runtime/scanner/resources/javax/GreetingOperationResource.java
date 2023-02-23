package test.io.smallrye.openapi.runtime.scanner.resources.javax;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
