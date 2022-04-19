package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "pets")
public class ResponseGenerationEnabledByIncompleteApiResponseTestResource {

    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "204", description = "Description 204")
    @APIResponse(responseCode = "400", description = "Description 400")
    public Pet createOrUpdatePet(Pet pet) {
        return pet;
    }

}
