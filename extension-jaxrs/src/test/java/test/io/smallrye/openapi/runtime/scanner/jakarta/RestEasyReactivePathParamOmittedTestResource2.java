package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "/movies")
public class RestEasyReactivePathParamOmittedTestResource2 {

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    public Widget get1(long id) {
        return null;
    }

}
