package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/english")
public class SalutationEnglish implements Salutation {

    @Override
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "hello";
    }

}
