package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.ArrayList;
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
