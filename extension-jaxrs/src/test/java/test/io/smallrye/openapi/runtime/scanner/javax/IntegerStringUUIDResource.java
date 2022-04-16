package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.UUID;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
