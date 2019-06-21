package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import test.io.smallrye.openapi.runtime.scanner.resources.RequestBodyTestApplication.DifferentObject;

/**
 * Test case for smallrye-open-api issue #116
 * https://github.com/smallrye/smallrye-open-api/issues/116
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
@OpenAPIDefinition(info = @Info(title = "Test Request Body", version = "1.0"), components = @Components(requestBodies = @RequestBody(name = "test", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DifferentObject.class)), required = true)))
public class RequestBodyTestApplication extends Application {

    public static class SomeObject {
        public String someName;
    }

    public static class DifferentObject {
        public String differentName;
        public UUID myId;
    }

    @SuppressWarnings("unused")
    @Path("requestbodies")
    public static class RequestBodyResource {

        @POST
        @Path("impl")
        public void testImpl(
                @RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DifferentObject.class)), required = true) SomeObject body) {
            return;
        }

        @POST
        @Path("ref")
        public void testRef(@RequestBody(ref = "test") SomeObject body) {
            return;
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Path("defaults")
        public void testDefaults(@RequestBody(required = true) SomeObject body) {
            return;
        }
    }
}
