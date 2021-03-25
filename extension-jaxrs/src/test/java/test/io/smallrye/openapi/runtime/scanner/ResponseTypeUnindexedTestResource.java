package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(value = "/unindexed")
public class ResponseTypeUnindexedTestResource {

    // This type will not be in the Jandex index, nor does it implement Map or List.
    static class ThirdPartyType {
    }

    @GET
    @Produces(value = MediaType.TEXT_PLAIN)
    public ThirdPartyType hello() {
        return null;
    }

}
