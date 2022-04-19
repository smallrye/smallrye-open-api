package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path(value = "/v1/magma")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class MagmaResource extends BaseResource<Magma> {

    @GET
    public ResultList<Magma> getAll(@QueryParam(value = "id") String id, @QueryParam(value = "limit") int limit,
            @QueryParam(value = "offset") int offset, @QueryParam(value = "orderby") List<String> orderBy) {
        return super.getAll1();
    }

    @POST
    @RequestBody(content = @Content(schema = @Schema(implementation = Magma.class)))
    public Result<Magma> post(Magma environment) {
        return super.post1(environment);
    }

    @PUT
    @RequestBody(content = @Content(schema = @Schema(implementation = Magma.class)))
    public Result<Magma> put(Magma environment) {
        return super.put1(environment);
    }

    @DELETE
    @RequestBody(content = @Content(schema = @Schema(implementation = Magma.class)))
    public Response delete(Magma environment) {
        return super.delete1(environment);
    }

}
