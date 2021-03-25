package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import org.eclipse.microprofile.openapi.annotations.Operation;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
@Path("/secret")
public class HiddenOperationResource {

    @GET
    @Operation(hidden = true)
    public String getHiddenResponse() {
        return "response value";
    }
}
