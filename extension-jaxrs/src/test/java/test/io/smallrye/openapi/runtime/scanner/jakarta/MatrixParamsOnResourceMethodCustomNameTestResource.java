package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import test.io.smallrye.openapi.runtime.scanner.Widget;

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
