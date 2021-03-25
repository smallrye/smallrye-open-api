package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path(value = "/v1")
public class PrimitiveArrayPolymorphismTestResource {

    @POST
    @Consumes(value = "application/json")
    @Produces(value = "application/json")
    @Operation(summary = "Convert an array of integer types to an array of floating point types")
    @RequestBody(content = @Content(schema = @Schema(anyOf = { int[].class, long[].class })))
    @APIResponses(value = {
            @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {
                    float[].class, double[].class }))) })
    public Object intToFloat(@SuppressWarnings(value = "unused") Object input) {
        return null;
    }

}
