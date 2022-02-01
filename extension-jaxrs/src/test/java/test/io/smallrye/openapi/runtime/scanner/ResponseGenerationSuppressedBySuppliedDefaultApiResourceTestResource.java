package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "pets")
public class ResponseGenerationSuppressedBySuppliedDefaultApiResourceTestResource {

    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", content = {}, description = "Description 200")
    @APIResponse(responseCode = "204", description = "Description 204")
    @APIResponse(responseCode = "400", description = "Description 400")
    public Pet createOrUpdatePet(Pet pet) {
        return pet;
    }

}
