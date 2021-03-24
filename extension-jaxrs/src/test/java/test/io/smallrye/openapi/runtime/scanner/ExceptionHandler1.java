package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Provider
public class ExceptionHandler1 implements ExceptionMapper<WebApplicationException> {

    @Override
    @APIResponse(responseCode = "500", description = "Server error")
    public Response toResponse(WebApplicationException e) {
        return null;
    }

}
