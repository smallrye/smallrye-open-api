package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@OpenAPIDefinition(info = @Info(title = "Test title", version = "0.1"), components = @Components(responses = {
        @APIResponse(responseCode = "404", description = "Not Found!", name = "NotFound"),
        @APIResponse(responseCode = "500", description = "Server Error!", name = "ServerError") }))
public class ReferenceResponseTestApp extends Application {

}
