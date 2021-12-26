package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.ArrayList;
import java.util.List;

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

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path(value = "/v1/kingcrimson")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class KingCrimsonResource extends BaseResource<KingCrimson> {

    @GET
    public ResultList<KingCrimson> getAll(@QueryParam(value = "id") String id, @QueryParam(value = "limit") int limit,
            @QueryParam(value = "offset") int offset, @QueryParam(value = "orderby") List<String> orderBy) {
        return super.getAll1();
    }

    @GET
    @Path(value = "/noooooooo")
    public Result<List<POJO>> getList() {
        return new Result.ResultBuilder<List<POJO>>().status(200).result(new ArrayList<>()).build();
    }

    @POST
    @RequestBody(content = @Content(schema = @Schema(implementation = KingCrimson.class)))
    public Result<KingCrimson> post(KingCrimson deployment) {
        return super.post1(deployment);
    }

    @PUT
    @RequestBody(content = @Content(schema = @Schema(implementation = KingCrimson.class)))
    public Result<KingCrimson> put(KingCrimson deployment) {
        return super.put1(deployment);
    }

    @DELETE
    @RequestBody(content = @Content(schema = @Schema(implementation = KingCrimson.class)))
    public Response delete(KingCrimson deployment) {
        return super.delete1(deployment);
    }

}
