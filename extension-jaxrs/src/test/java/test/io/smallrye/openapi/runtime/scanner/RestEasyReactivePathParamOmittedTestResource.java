package test.io.smallrye.openapi.runtime.scanner;

import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.jboss.resteasy.reactive.RestPath;

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
