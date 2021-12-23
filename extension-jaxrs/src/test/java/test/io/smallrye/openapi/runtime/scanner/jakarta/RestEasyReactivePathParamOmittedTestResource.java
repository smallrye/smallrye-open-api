package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.validation.constraints.Min;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.jboss.resteasy.reactive.RestPath;

import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "/path/{param1}")
public class RestEasyReactivePathParamOmittedTestResource {

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path(value = "/params/{param2}")
    public Widget get1(@Min(value = 100) int param1, @RestPath String param2) {
        return null;
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path(value = "/params/{param3}")
    public Widget get2(@Extension(name = "custom-info", value = "value for param3") String param3, @Context Request param1,
            int paramOne) {
        return null;
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path(value = "/params/{param4}")
    public Widget get3(int param1, String param4) {
        return null;
    }
}
