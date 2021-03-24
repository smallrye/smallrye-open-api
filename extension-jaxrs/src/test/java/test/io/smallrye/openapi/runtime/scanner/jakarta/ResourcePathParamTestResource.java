package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "/parameter-on-field/{id}")
public class ResourcePathParamTestResource {

    @PathParam(value = "id")
    @DefaultValue(value = "ABC")
    String id;

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public Widget get() {
        return null;
    }

}
