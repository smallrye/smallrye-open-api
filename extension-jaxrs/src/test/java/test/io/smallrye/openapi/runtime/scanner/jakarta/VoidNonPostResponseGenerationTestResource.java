package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
