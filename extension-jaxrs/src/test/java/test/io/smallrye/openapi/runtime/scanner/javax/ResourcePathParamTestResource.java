package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
