package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

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
