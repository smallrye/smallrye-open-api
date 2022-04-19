package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

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
