package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "pets")
public class VoidPostResponseGenerationTestResource {

    @SuppressWarnings(value = "unused")
    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "201")
    @APIResponse(responseCode = "400", description = "Description 400")
    public void createOrUpdatePet(Pet pet) {
    }

}
