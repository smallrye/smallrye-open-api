package test.io.smallrye.openapi.runtime.scanner;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path(value = "/generic")
@Consumes(value = MediaType.APPLICATION_JSON)
@Produces(value = MediaType.APPLICATION_JSON)
public class TestResource3 extends BaseResource2<Apple, String> {

    @POST
    @Path(value = "save")
    public Apple update(Apple filter) {
        return null;
    }

    @POST
    @Path(value = "retrieve")
    public List<Apple> update(
            @QueryParam("ids") @Schema(type = SchemaType.ARRAY, implementation = Integer.class) String values) {
        return null;
    }

}
