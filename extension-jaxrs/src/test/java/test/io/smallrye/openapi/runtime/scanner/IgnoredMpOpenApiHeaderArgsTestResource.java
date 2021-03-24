package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(value = "ignored-headers")
public class IgnoredMpOpenApiHeaderArgsTestResource {

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @SuppressWarnings(value = "unused")
    public Widget get(@HeaderParam(value = "Authorization") String auth,
            @HeaderParam(value = "Content-Type") String contentType, @HeaderParam(value = "Accept") String accept,
            @HeaderParam(value = "X-Custom-Header") String custom) {
        return null;
    }

}
