package test.io.smallrye.openapi.runtime.scanner;

import javax.validation.constraints.Size;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path(value = "/matrix-params-on-resource-method-custom-name/{id}")
@SuppressWarnings(value = "unused")
public class MatrixParamsOnResourceMethodCustomNameTestResource {

    @PathParam(value = "id")
    @Size(max = 10)
    String id;

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Parameter(name = "id", in = ParameterIn.PATH, style = ParameterStyle.MATRIX, description = "Additional information for id2")
    public Widget get(@MatrixParam(value = "m1") @DefaultValue(value = "default-m1") String m1,
            @MatrixParam(value = "m2") @Size(min = 20) String m2) {
        return null;
    }

}
