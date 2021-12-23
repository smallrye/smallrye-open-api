package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path(value = "/v1")
public class PrimitiveArraySchemaTestResource {

    @Schema(name = "PrimitiveArrayTestObject", description = "the REST response class")
    public static class PrimitiveArrayTestObject {

        @Schema(required = true, description = "a packed data array")
        private double[] data;
        @Schema(implementation = double.class, type = SchemaType.ARRAY)
        private float[] data2;
    }

    @GET
    @Operation(summary = "Get an object containing a primitive array")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrimitiveArrayTestObject.class))) })
    public PrimitiveArrayTestObject getResponse() {
        return new PrimitiveArrayTestObject();
    }

}
