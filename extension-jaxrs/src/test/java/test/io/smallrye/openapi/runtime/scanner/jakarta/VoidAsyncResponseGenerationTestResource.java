package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import test.io.smallrye.openapi.runtime.scanner.ServerError;

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
