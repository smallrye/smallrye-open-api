package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import jakarta.ws.rs.GET;

@Path("/multi-produce-consume")
public class MultiProduceConsumeResource {

    public static final String COMMON = "application/json, application/xml";

    @Produces({ COMMON, "text/plain", "text/html" })
    @Consumes({ "text/plain", "text/html", COMMON })
    @POST
    public Object testCsvs(Object body) {
        return null;
    }

    @Produces(" text/plain ")
    @GET
    public Object testSpaces() {
        return null;
    }

}
