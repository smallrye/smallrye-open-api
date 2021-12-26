package test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
