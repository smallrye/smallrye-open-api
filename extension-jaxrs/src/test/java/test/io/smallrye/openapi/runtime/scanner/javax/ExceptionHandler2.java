package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Provider
public class ExceptionHandler2 implements ExceptionMapper<NotFoundException> {

    @Override
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response toResponse(NotFoundException e) {
        return null;
    }

}
