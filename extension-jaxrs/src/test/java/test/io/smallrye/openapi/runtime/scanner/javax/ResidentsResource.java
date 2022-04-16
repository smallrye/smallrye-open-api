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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "/v1/residents")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class ResidentsResource extends BaseResource<Residents> {

    @GET
    public ResultList<Residents> getAll(@QueryParam(value = "id") String id, @QueryParam(value = "limit") int limit,
            @QueryParam(value = "offset") int offset, @QueryParam(value = "orderby") List<String> orderBy) {
        return super.getAll1();
    }

    @POST
    @RequestBody(content = @Content(schema = @Schema(implementation = Residents.class)))
    @APIResponse(responseCode = "200", description = "Creates a new Chart")
    public Result<Residents> post(Residents chart) {
        return super.post1(chart);
    }

    @PUT
    @RequestBody(content = @Content(schema = @Schema(implementation = Residents.class)))
    @APIResponse(responseCode = "200", description = "Creates a new Chart")
    public Result<Residents> put(Residents chart) {
        return super.put1(chart);
    }

    @DELETE
    @RequestBody(content = @Content(schema = @Schema(implementation = Residents.class)))
    public Response delete(Residents chart) {
        return super.delete1(chart);
    }

}
