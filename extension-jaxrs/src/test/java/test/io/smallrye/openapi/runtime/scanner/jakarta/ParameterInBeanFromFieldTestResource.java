package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import test.io.smallrye.openapi.runtime.scanner.Widget;

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
