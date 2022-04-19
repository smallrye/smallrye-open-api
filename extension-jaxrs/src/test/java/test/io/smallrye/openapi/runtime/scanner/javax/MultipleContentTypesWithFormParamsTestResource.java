package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import test.io.smallrye.openapi.runtime.scanner.Widget;

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
