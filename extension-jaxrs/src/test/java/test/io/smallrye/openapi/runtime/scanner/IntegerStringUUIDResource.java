package test.io.smallrye.openapi.runtime.scanner;

import java.util.UUID;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(value = "/integer-string")
@Consumes(value = MediaType.APPLICATION_JSON)
@Produces(value = MediaType.APPLICATION_JSON)
public class IntegerStringUUIDResource extends BaseGenericResource<Integer, String, UUID> {

    @POST
    @Path(value = "save")
    public Integer update(Integer value, @BeanParam GenericBean<String> gbean) {
        return null;
    }

}
