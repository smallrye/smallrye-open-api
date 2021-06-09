package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface Salutation {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get();

}
