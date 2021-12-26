package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "pets")
public class ReferenceResponseTestResource {

    @SuppressWarnings(value = "unused")
    @Path(value = "{id}")
    @GET
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200")
    @APIResponse(ref = "NotFound")
    @APIResponse(ref = "ServerError")
    public JsonObject getPet(@PathParam(value = "id") String id) {
        return null;
    }

}
