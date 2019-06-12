package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
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

        @JsonbProperty("will_not_be_used")
        @Schema(name = "another_child", description = "This schema and description will be replaced with a $ref to 'another_nested', but the name will be used for the property")
        AnotherNestedChildWithSchemaName another;

        List<NestedParameterTestChild> childList;

        Map<String, NestedParameterTestChild> childMap;
    }

    @Schema(description = "The description of the child")
    public static class NestedParameterTestChild {
        @Schema(required = true)
        String id;
        String name;
    }

    @Dependent
    @Schema(name = "another_nested", description = "The name of this child is not 'AnotherNestedChildWithSchemaName'")
    public static class AnotherNestedChildWithSchemaName {
        @Schema(required = true)
        String id;
        @Schema(name = "name_", title = "This property's 'name' has been overridden using the @Schema")
        String name;
    }

    @SuppressWarnings("unused")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = NestedParameterTestParent.class)))
    public NestedParameterTestParent getHiddenResponse(@Parameter(name = "arg", in = ParameterIn.COOKIE) NestedParameterTestParent request) {
        return new NestedParameterTestParent();
    }
}
