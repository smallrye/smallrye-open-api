package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/multi-produce-consume")
public class MultiProduceConsumeResourceJakarta {

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
