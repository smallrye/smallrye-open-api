package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "/parameter-in-bean-from-setter/{id}/{id2}")
@SuppressWarnings(value = "unused")
public class ParameterInBeanFromSetterTestResource {

    public static class Bean {

        @PathParam(value = "id")
        @DefaultValue(value = "BEAN-FROM-SETTER")
        String id;
    }

    private Bean param;

    @BeanParam
    public void setParam(Bean param) {
        this.param = param;
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public Widget get(@PathParam(value = "id2") String id2) {
        return null;
    }

}
