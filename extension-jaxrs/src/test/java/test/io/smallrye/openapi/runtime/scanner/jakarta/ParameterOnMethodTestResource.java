package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "parameter-on-method/{id}")
public class ParameterOnMethodTestResource {

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @SuppressWarnings(value = "unused")
    @Parameter(name = "X-Custom-Header", in = ParameterIn.HEADER, required = true)
    @Parameter(name = "id", in = ParameterIn.PATH)
    public Widget get(@HeaderParam(value = "X-Custom-Header") String custom,
            @PathParam(value = "id") @DefaultValue(value = "000") String id) {
        return null;
    }

}
