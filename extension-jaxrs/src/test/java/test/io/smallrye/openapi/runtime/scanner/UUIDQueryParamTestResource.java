package test.io.smallrye.openapi.runtime.scanner;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path(value = "/uuid")
public class UUIDQueryParamTestResource {

    public static class WrappedUUID {

        @Schema(format = "uuid", description = "test")
        UUID theUUID;
    }

    @GET
    @Produces(value = MediaType.TEXT_PLAIN)
    public WrappedUUID[] echoWrappedUUID(@QueryParam(value = "val") UUID value) {
        WrappedUUID result = new WrappedUUID();
        result.theUUID = value;
        return new WrappedUUID[] { result };
    }

    @POST
    @Consumes(value = MediaType.TEXT_PLAIN)
    @Produces(value = MediaType.TEXT_PLAIN)
    public UUID echoPostedUUID(UUID value) {
        return value;
    }

}
