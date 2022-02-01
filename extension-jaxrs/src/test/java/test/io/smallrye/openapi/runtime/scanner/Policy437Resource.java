package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path(value = "/")
public class Policy437Resource {

    @GET
    @Path(value = "/beanparamimpl")
    public Policy437 getWithBeanParams() {
        return null;
    }

}
