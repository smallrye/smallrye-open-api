package test.io.smallrye.openapi.runtime.scanner;

import java.util.UUID;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
