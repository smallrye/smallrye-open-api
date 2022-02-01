package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

@Path(value = "pets")
public class ResponseMultipartGenerationTestResource {

    @GET
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = "multipart/mixed")
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "400", description = "Description 400")
    public MultipartOutput getPetWithPicture() {
        return null;
    }

}
