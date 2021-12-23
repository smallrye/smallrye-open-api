package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "/path-param-with-form-params/{id}")
@SuppressWarnings(value = "unused")
public class PathParamWithFormParamsTestResource {

    @PathParam(value = "id")
    @DefaultValue(value = "12345")
    @NotNull
    @Size(min = 1, max = 12)
    String id;
    @FormParam(value = "form-param1")
    private String formParam1;

    @POST
    @Consumes(value = MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(value = MediaType.APPLICATION_JSON)
    public Widget update(@FormParam(value = "form-param2") @Size(max = 10) String formParam2,
            @FormParam(value = "qualifiers") java.util.SortedSet<String> qualifiers) {
        return null;
    }

}
