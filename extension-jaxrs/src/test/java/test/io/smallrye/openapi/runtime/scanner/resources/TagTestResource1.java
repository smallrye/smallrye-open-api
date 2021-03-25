package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/tags1")
@Tag(name = "tag3", description = "TAG3 from TagTestResource1")
@SuppressWarnings("unused")
public class TagTestResource1 {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getValue1() {
        return null;
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Tag(name = "tag1", description = "TAG1 from TagTestResource1#postValue")
    void postValue(String value) {
    }

    @PATCH
    @Consumes(MediaType.TEXT_PLAIN)
    @Tag
    void patchValue(String value) {
    }
}
