package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path(value = "/override")
public class ParamNameOverrideTestResource {

    @HeaderParam(value = "h1")
    @Parameter(name = "X-Header1", in = ParameterIn.HEADER, content = {})
    String h1;
    @CookieParam(value = "c1")
    @Parameter(name = "Cookie1", in = ParameterIn.COOKIE, content = @Content(mediaType = MediaType.TEXT_PLAIN))
    String c1;
    @QueryParam(value = "q1")
    @Parameter(name = "query1", in = ParameterIn.QUERY, content = {
            @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(description = "A JSON query parameter", type = SchemaType.OBJECT)),
            @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(description = "A plain text query parameter", type = SchemaType.STRING)) })
    String q1;

    @GET
    @Path(value = "{p1}")
    @Produces(value = MediaType.TEXT_PLAIN)
    public String echo(
            @Parameter(name = "Path1", in = ParameterIn.PATH, style = ParameterStyle.SIMPLE, description = "The name 'Path1' will not be used instead of 'p1'") @PathParam(value = "p1") String p1) {
        return p1;
    }

}
