package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.json.JsonObject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(value = "/greet")
public class GreetResource {

    @Path(value = "/{name}")
    @GET
    @Operation(summary = "Returns a personalized greeting")
    @APIResponse(description = "Simple JSON containing the greeting", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GreetingMessage.class)))
    @Produces(value = MediaType.APPLICATION_JSON)
    public JsonObject getMessage(@Parameter(description = "The greeting name") @BeanParam @PathParam(value = "name") String name) {
        return null;
    }

    public static class GreetingMessage {

        String message;
    }

}
