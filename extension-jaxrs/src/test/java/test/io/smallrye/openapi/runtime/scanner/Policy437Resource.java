package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path(value = "/")
public class Policy437Resource {

    @GET
    @Path(value = "/beanparamimpl")
    public Policy437 getWithBeanParams() {
        return null;
    }

}
