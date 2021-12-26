package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import test.io.smallrye.openapi.runtime.scanner.Apple;

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
