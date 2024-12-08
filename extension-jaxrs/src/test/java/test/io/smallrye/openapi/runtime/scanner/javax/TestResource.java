package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

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

    @ServerExceptionMapper
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response handleNotFound(NotFoundException exception) {
        return exception.getResponse();
    }
}
