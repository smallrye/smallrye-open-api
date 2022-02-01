package test.io.smallrye.openapi.runtime.scanner;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(value = "/matrix-params-on-resource-method-args/{id}")
@SuppressWarnings(value = "unused")
public class MatrixParamsOnResourceMethodArgsTestResource {

    @PathParam(value = "id")
    @NotNull
    @Size(max = 10)
    String id;

    @GET
    @Path(value = "/anotherpathsegment/reloaded/")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Widget get(@MatrixParam(value = "m1") @DefaultValue(value = "default-m1") String m1,
            @MatrixParam(value = "m2") @Size(min = 20) String m2) {
        return null;
    }

}
