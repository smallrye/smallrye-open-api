package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public interface Conversation {

    @POST
    @Path("/speak")
    @Consumes(value = MediaType.TEXT_PLAIN)
    @Produces(value = MediaType.TEXT_PLAIN)
    default String speak(String message) {
        return "<silence>";
    }

}
