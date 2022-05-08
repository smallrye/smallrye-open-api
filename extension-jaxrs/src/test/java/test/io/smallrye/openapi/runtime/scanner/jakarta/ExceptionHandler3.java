package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@APIResponse(responseCode = "500", description = "Internal Server Error")
@APIResponse(responseCode = "400", description = "Bad Request")
public class ExceptionHandler3 implements ExceptionMapper<WebApplicationException> {

    @Override
    @APIResponse(responseCode = "503", description = "Service Unavailable")
    @APIResponse(responseCode = "500", description = "Unexpected Error") // Method annotation should override class
    public Response toResponse(WebApplicationException exception) {
        return null;
    }

}
