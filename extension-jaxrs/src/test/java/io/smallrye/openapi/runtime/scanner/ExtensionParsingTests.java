package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callbacks;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.json.JSONException;
import org.junit.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 * 
 */
public class ExtensionParsingTests extends IndexScannerTestBase {

    @Test
    public void testAllExpectedParseTypes() throws IOException, JSONException {
        assertJsonEquals("extensions.parsing.expected.json",
                         ExtensionParsingTestResource.class);
    }

    /* Test models and resources below. */

    @Path("/ext")
    static class ExtensionParsingTestResource {
        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        @Callbacks({
                     @Callback(
                             name = "extendedCallback",
                             callbackUrlExpression = "http://localhost:8080/resources/ext-callback",
                             operations = @CallbackOperation(
                                     summary = "Get results",
                                     extensions = {
                                                    @Extension(name = "x-object", value = "{ \"key\":\"value\" }", parseValue = true),
                                                    @Extension(name = "x-object-unparsed", value = "{ \"key\":\"value\" }"),
                                                    @Extension(name = "x-array", value = "[ \"val1\",\"val2\" ]", parseValue = true),
                                                    @Extension(name = "x-booltrue", value = "true", parseValue = true),
                                                    @Extension(name = "x-boolfalse", value = "false", parseValue = true),
                                                    @Extension(name = "x-number", value = "42", parseValue = true),
                                                    @Extension(name = "x-number-sci", value = "42e55", parseValue = true),
                                                    @Extension(name = "x-positive-number-remains-string", value = "+42", parseValue = true),
                                                    @Extension(name = "x-negative-number", value = "-42", parseValue = true),
                                                    @Extension(name = "x-unparsable-number", value = "-Not.A.Number", parseValue = true)
                                     },
                                     method = "get",
                                     responses = @APIResponse(
                                             responseCode = "200",
                                             description = "successful operation",
                                             content = @Content(
                                                     mediaType = "application/json",
                                                     schema = @Schema(type = SchemaType.ARRAY, implementation = String.class)))))
        })
        public String get(String data) {
            return data;
        }
    }

    @Test
    public void testSiblingExtensionAnnotations() throws IOException, JSONException {
        assertJsonEquals("extensions.scan-siblings.expected.json",
                         ExtensionPlacementTestResource.class,
                         ExtensionPlacementTestResource.Model.class);
    }

    @Path("/ext")
    static class ExtensionPlacementTestResource {
        @Schema
        @Extension(name = "model-schema-ext", value = "{ \"key\":\"value\" }", parseValue = true)
        static class Model {
            @Schema
            @Extension(name = "value1-ext", value = "plain string", parseValue = true)
            String value1;
            @Schema
            @Extension(name = "value2-ext", value = "plain string", parseValue = false)
            Integer value2;
        }

        @GET
        @Path("segment1")
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        @Extension(name = "operation-ext", value = "plain string")
        public Model get(@QueryParam("data") @Parameter() @Extension(
                name = "qparam-data-ext",
                value = "1",
                parseValue = true) String data) {
            return null;
        }
    }
}
