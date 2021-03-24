package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "/pets")
public class DiscriminatorNoMappingTestResource {

    @Path(value = "{id}")
    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns an AbstractPet with only a discriminator property declared in the response, "
            + "no Dogs allowed!")
    @APIResponse(content = {
            @Content(schema = @Schema(oneOf = { Cat.class, Lizard.class }, discriminatorProperty = "pet_type")) })
    @SuppressWarnings(value = "unused")
    public AbstractPet get(@PathParam(value = "id") String id) {
        return null;
    }

}
