package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
