package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
@Path("/nested")
public class NestedSchemaOnParameterResource {

    public static class NestedParameterTestParent {
        @Schema(required = true)
        String id;
        @Schema(required = true)
        String name;
        @Schema(required = true)
        NestedParameterTestChild nested;

        List<NestedParameterTestChild> childList;

        Map<String, NestedParameterTestChild> childMap;
    }

    @Schema(description = "The description of the child")
    public static class NestedParameterTestChild {
        @Schema(required = true)
        String id;
        String name;
    }

    @SuppressWarnings("unused")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = NestedParameterTestParent.class)))
    public NestedParameterTestParent getHiddenResponse(@Parameter(name = "arg") NestedParameterTestParent request) {
        return new NestedParameterTestParent();
    }
}
