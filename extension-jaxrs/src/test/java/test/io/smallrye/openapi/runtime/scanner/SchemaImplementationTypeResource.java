package test.io.smallrye.openapi.runtime.scanner;

import java.net.URI;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@Path(value = "/hello")
public class SchemaImplementationTypeResource {

    public static class GreetingMessage {

        @Schema(description = "Used to send a message")
        private final SimpleString message;
        @Schema(implementation = String.class, description = "Simply a string", required = false)
        private SimpleString optionalMessage;

        public GreetingMessage(@JsonProperty SimpleString message) {
            this.message = message;
        }

        public SimpleString getMessage() {
            return message;
        }

        public SimpleString getOptionalMessage() {
            return optionalMessage;
        }
    }

    @Schema(implementation = String.class, title = "A Simple String")
    public static class SimpleString {

        @Schema(hidden = true)
        private final String value;

        public SimpleString(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    @SuppressWarnings(value = "unused")
    @POST
    @Consumes(value = "application/json")
    @Produces(value = "application/json")
    public Response doPost(GreetingMessage message) {
        return Response.created(URI.create("http://example.com")).build();
    }

}
