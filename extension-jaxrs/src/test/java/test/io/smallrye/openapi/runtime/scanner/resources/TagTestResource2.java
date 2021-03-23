package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("/tags2")
@Tag(description = "This tag will not appear without a name")
@Tag(name = "tag1", description = "TAG1 from TagTestResource2")
@Tag(ref = "http://example/com/tag2")
@SuppressWarnings("unused")
public class TagTestResource2 {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Tag(name = "tag3", description = "TAG3 from TagTestResource2#getValue1", externalDocs = @ExternalDocumentation(description = "Ext doc from TagTestResource2#getValue1"))
    String getValue1() {
        return null;
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    void postValue(String value) {
    }

    @PATCH
    @Consumes(MediaType.TEXT_PLAIN)
    @Tags({
            @Tag, @Tag
    })
    void patchValue(String value) {
    }
}
