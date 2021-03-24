package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

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
