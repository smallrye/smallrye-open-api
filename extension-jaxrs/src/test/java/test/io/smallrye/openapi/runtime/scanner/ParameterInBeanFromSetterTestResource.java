package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
