package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path(value = "/common-target-method")
public class CommonTargetMethodParameterResource {

    @DefaultValue(value = "10")
    @QueryParam(value = "limit")
    @Parameter(description = "Description of the limit query parameter")
    public void setLimit(int limit) {
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public String[] getRecords() {
        return null;
    }

}
