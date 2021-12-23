package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
@Path("/public")
public class VisibleOperationResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Operation
    @APIResponse(responseCode = "200", description = "A successful response", content = @Content(schema = @Schema(type = SchemaType.STRING)))
    public String getPublicResponse() {
        return "response value";
    }
}
