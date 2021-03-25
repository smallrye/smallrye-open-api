package test.io.smallrye.openapi.runtime.scanner;

import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "/greet")
public class GreetResource {

    @Path(value = "/{name}")
    @GET
    @Operation(summary = "Returns a personalized greeting")
    @APIResponse(description = "Simple JSON containing the greeting", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GreetingMessage.class)))
    @Produces(value = MediaType.APPLICATION_JSON)
    public JsonObject getMessage(@Parameter(description = "The greeting name") @PathParam(value = "name") String name) {
        return null;
    }

    public static class GreetingMessage {

        String message;
    }

}
