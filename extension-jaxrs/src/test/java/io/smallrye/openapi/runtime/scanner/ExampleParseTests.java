package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

class ExampleParseTests extends IndexScannerTestBase {

    @Test
    void testParametersExamplesParsedWhenJson() throws IOException, JSONException {
        @jakarta.ws.rs.Path("examples")
        class ExampleResource {
            @Parameter(example = "2019-05-02T09:51:25.265", examples = {
                    @ExampleObject(name = "datetime", value = "2099-12-31T23:59:59.999")
            })
            @jakarta.ws.rs.QueryParam("createDateTimeMax")
            public LocalDateTime createDateTimeMax;

            @Parameter(schema = @Schema(type = SchemaType.OBJECT), example = "{ \"key\": \"value\" }", examples = {
                    @ExampleObject(name = "json", value = "{ \"key\": \"value\" }")
            })
            @jakarta.ws.rs.QueryParam("encodedJson")
            public Object encodedJson;

            @Parameter(schema = @Schema(type = SchemaType.STRING), example = "\"key\": \"value\"", examples = {
                    @ExampleObject(name = "keyValuePair", value = "\"key\": \"value\"")
            })
            @jakarta.ws.rs.QueryParam("keyValuePair")
            public Object keyValuePair;

            @Parameter(example = "3.1415")
            @jakarta.ws.rs.QueryParam("floatingpoint")
            public Float floatingpoint;

            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
            public jakarta.ws.rs.core.Response getExamples() {
                return null;
            }
        }

        assertJsonEquals("examples.parameters.json", ExampleResource.class);
    }

    @Test
    void testResponseContentExampleParsedWhenJson() throws IOException, JSONException {
        @jakarta.ws.rs.Path("examples")
        class ExampleResource {
            final String exampleIds = "1200635948\n" +
                    "1201860613\n" +
                    "1201901219";

            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Produces({
                    jakarta.ws.rs.core.MediaType.TEXT_PLAIN,
                    jakarta.ws.rs.core.MediaType.APPLICATION_JSON,
            })
            @APIResponse(responseCode = "200", content = {
                    @Content(mediaType = jakarta.ws.rs.core.MediaType.TEXT_PLAIN, example = exampleIds, examples = {
                            @ExampleObject(name = "identifiers", value = exampleIds),
                    }),
                    @Content(mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_JSON, example = "[ \"123\", \"456\" ]", examples = {
                            @ExampleObject(name = "identifiers", value = "[ \"1200635948\", \"1201860613\", \"1201901219\" ]"),
                    })
            })
            @APIResponse(responseCode = "206", description = "Partial Content", content = {
                    @Content(example = "1", examples = {
                            @ExampleObject(name = "integer", value = "1"),
                    }),
            })
            public jakarta.ws.rs.core.Response getExamples() {
                return null;
            }
        }

        assertJsonEquals("examples.responses.json", ExampleResource.class);
    }
}
