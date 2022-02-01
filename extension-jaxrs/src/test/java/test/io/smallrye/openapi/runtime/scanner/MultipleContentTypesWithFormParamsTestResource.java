package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path(value = "/multiple-content-types-with-form-params")
@SuppressWarnings(value = "unused")
public class MultipleContentTypesWithFormParamsTestResource {

    @POST
    @Path(value = "/widgets/create")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Operation(operationId = "createWidget")
    public void createWidget(
            @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Widget.class))) final Widget w) {
    }

    @POST
    @Path(value = "/widgets/create")
    @Consumes(value = MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(operationId = "createWidget")
    public void createWidget(@FormParam(value = "id") String id, @FormParam(value = "name") String name) {
    }

}
