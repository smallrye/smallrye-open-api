package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "pets")
public class ResponseGenerationJsonExampleApiResourceTestResource {

    @GET
    @Path("/{id}")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
            @ExampleObject(name = "response_success_1", value = "{\"key\":\"value\"}"),
            @ExampleObject(name = "response_success_2", value = "{\"key2\":12}")
    }, example = "{\"key\":\"value\"}"), description = "Description 200")
    public Pet getPet(@Parameter(name = "id", example = "{\"key\":\"value\"}") String id) {
        return null;
    }

}
