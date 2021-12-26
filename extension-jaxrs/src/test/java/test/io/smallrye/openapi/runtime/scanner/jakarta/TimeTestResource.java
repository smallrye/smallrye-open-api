package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path(value = "/times")
@Produces(value = MediaType.TEXT_PLAIN)
public class TimeTestResource {

    public static class UTC {

        @Schema(description = "Current time at offset '00:00'")
        OffsetTime utc = OffsetTime.now(ZoneId.of("UTC"));
    }

    @Path(value = "local")
    @GET
    public LocalTime getLocalTime() {
        return LocalTime.now();
    }

    @Path(value = "zoned")
    @GET
    public OffsetTime getZonedTime(@QueryParam(value = "zoneId") String zoneId) {
        return OffsetTime.now(ZoneId.of(zoneId));
    }

    @Path(value = "utc")
    @GET
    public UTC getUTC() {
        return new UTC();
    }

    @Path(value = "utc")
    @POST
    public OffsetTime toUTC(@QueryParam(value = "local") LocalTime local, @QueryParam(value = "offsetId") String offsetId) {
        return OffsetTime.of(local, ZoneOffset.of(offsetId));
    }

}
