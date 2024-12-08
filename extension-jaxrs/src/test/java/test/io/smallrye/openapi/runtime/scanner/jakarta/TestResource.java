package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

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
