package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "resource2s")
public class TestResource2 {

    @GET
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    @APIResponse(responseCode = "404", description = "Resource Not Found")
    public String getResource() throws NotFoundException {
        return "resource2";
    }

    @POST
    public String createResource() throws WebApplicationException {
        return "OK";
    }

}
