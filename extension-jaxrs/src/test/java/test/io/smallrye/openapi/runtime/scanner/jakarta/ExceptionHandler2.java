package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler2 implements ExceptionMapper<NotFoundException> {

    @Override
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response toResponse(NotFoundException e) {
        return null;
    }

}
