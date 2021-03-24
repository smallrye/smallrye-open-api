package test.io.smallrye.openapi.runtime.scanner;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
