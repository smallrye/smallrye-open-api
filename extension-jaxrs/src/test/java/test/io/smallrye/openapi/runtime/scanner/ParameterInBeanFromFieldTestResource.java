package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(value = "/parameter-in-bean-from-field/{id}")
public class ParameterInBeanFromFieldTestResource {

    public static class Bean {

        @PathParam(value = "id")
        @DefaultValue(value = "BEAN")
        String id;
    }

    @BeanParam
    private Bean param;

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public Widget get() {
        return null;
    }

}
