package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callbacks;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/* Test models and resources below. */
@Path(value = "/ext-custom")
public class ExtensionParsingTestResource1 {

    @POST
    @Consumes(value = MediaType.TEXT_PLAIN)
    @Produces(value = MediaType.TEXT_PLAIN)
    @Callbacks(value = {
            @Callback(name = "extendedCallback", callbackUrlExpression = "http://localhost:8080/resources/ext-callback", operations = @CallbackOperation(summary = "Get results", extensions = {
                    @Extension(name = "x-object", value = "{ \"key\":\"value\" }", parseValue = true),
                    @Extension(name = "x-object-unparsed", value = "{ \"key\":\"value\" }"),
                    @Extension(name = "x-array", value = "[ \"val1\",\"val2\" ]", parseValue = true),
                    @Extension(name = "x-booltrue", value = "true", parseValue = false) }, method = "get", responses = @APIResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = String.class))))) })
    public String get(String data) {
        return data;
    }

}
