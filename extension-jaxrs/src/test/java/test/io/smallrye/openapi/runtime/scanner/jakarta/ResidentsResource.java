package test.io.smallrye.openapi.runtime.scanner.jakarta;

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
