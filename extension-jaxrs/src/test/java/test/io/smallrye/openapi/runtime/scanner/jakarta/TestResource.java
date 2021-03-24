package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

@Path(value = "/resources")
public class TestResource {

    @GET
    public String getResource() throws NotFoundException {
        return "resource";
    }

    @POST
    public String createResource() throws WebApplicationException {
        return "OK";
    }

}
