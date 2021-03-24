package test.io.smallrye.openapi.runtime.scanner;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
