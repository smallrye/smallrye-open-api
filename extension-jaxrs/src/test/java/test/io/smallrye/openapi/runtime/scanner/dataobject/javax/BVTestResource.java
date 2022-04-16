package test.io.smallrye.openapi.runtime.scanner.dataobject.javax;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(value = "/bv")
public class BVTestResource {

    @SuppressWarnings(value = "unused")
    @Path(value = "/test-container")
    @POST
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Tag(name = "Test", description = "Testing the container")
    public BVTestContainer getTestContainer(BVTestResourceEntity parameter) {
        return new BVTestContainer();
    }

}
