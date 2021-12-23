package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path(value = "/v1")
public class PrimitiveArrayParameterTestResource {

    @POST
    @Consumes(value = "application/json")
    @Produces(value = "application/json")
    @Operation(summary = "Convert an array of doubles to an array of floats")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = float[].class))) })
    public float[] doubleToFloat(@SuppressWarnings(value = "unused") double[] input) {
        return new float[0];
    }

}
