package test.io.smallrye.openapi.runtime.scanner;

import javax.validation.constraints.Max;
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
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

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
