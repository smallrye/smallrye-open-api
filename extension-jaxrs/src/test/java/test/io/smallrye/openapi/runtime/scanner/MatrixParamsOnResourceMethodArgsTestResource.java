package test.io.smallrye.openapi.runtime.scanner;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
