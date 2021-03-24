package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

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
