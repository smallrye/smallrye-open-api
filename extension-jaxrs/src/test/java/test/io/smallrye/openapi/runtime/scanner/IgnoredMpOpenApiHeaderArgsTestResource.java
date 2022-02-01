package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
