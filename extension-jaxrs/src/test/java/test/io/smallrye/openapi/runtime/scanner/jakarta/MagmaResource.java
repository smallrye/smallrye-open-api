package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
