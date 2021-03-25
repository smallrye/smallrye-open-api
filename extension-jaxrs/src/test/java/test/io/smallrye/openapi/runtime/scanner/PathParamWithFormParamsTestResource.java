package test.io.smallrye.openapi.runtime.scanner;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
