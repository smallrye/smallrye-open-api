package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "pets/{id}")
@Consumes(value = MediaType.APPLICATION_JSON)
@Produces(value = MediaType.APPLICATION_JSON)
public class VoidAsyncResponseGenerationTestResource {

    @SuppressWarnings(value = "unused")
    @GET
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "400", description = "Description 400")
    @APIResponse(description = "Server Error: 500", responseCode = "500", content = @Content(schema = @Schema(implementation = ServerError.class)))
    public void getPet(@PathParam(value = "id") String id, @Suspended AsyncResponse response) {
    }

    @SuppressWarnings(value = "unused")
    @DELETE
    public void deletePet(@PathParam(value = "id") String id) {
    }

    @SuppressWarnings(value = "unused")
    @DELETE
    @Path(value = "async")
    public void deletePetAsync(@PathParam(value = "id") String id, @Suspended AsyncResponse response) {
    }

}
