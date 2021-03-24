package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "/matrix-params-on-method-and-field-args/{id}")
@SuppressWarnings(value = "unused")
public class MatrixParamsOnMethodAndFieldArgsTestResource {

    @PathParam(value = "id")
    @Size(max = 10)
    String id;
    @MatrixParam(value = "c1")
    @Schema(type = SchemaType.STRING, format = "custom-but-ignored")
    String c1;
    @MatrixParam(value = "c2")
    String c2;

    @GET
    @Path(value = "/seg1/seg2/resourceA")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Parameter(in = ParameterIn.PATH, name = "resourceA", style = ParameterStyle.MATRIX)
    public Widget get(@MatrixParam(value = "m1") @DefaultValue(value = "default-m1") int m1,
            @MatrixParam(value = "m2") @DefaultValue(value = "100") @Max(value = 200) int m2) {
        return null;
    }

}
