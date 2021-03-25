package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path(value = "/fruits")
public class FruitResource2 {

    @GET
    @Path(value = "/{fid}/notes/{nid}")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Note.class))),
            @APIResponse(responseCode = "404", description = "Not Found - The `Fruit` or `Note` could not be found.") })
    public Response getNote(@PathParam(value = "fid") final String fid, @PathParam(value = "nid") final String nid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Produces(value = MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

}
