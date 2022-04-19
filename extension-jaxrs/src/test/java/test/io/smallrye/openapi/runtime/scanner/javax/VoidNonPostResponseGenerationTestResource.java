package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "pets")
public class VoidNonPostResponseGenerationTestResource {

    @SuppressWarnings(value = "unused")
    @Path(value = "{id}")
    @DELETE
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "400", description = "Description 400")
    public void deletePet(@PathParam(value = "id") String id) {
    }

}
