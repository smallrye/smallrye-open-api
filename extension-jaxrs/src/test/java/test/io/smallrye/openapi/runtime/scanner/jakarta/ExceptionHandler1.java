package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler1 implements ExceptionMapper<WebApplicationException> {

    @Override
    @APIResponse(responseCode = "500", description = "Server error")
    public Response toResponse(WebApplicationException e) {
        return null;
    }

}
