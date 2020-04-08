package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
