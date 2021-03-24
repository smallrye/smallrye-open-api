package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

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
