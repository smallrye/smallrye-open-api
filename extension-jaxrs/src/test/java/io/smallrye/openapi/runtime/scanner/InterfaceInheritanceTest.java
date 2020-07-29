package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InterfaceInheritanceTest extends IndexScannerTestBase {

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #423.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/423
     *
     */
    @Test
    public void testInterfaceInheritance() throws IOException, JSONException {
        Index i = indexOf(ImmutableEntity.class, MutableEntity.class, Note.class, FruitResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.interface-inheritance.json", result);
    }

    static interface ImmutableEntity {
        @Schema(example = "0", required = true, description = "When the entity was created as a Unix timestamp. For create operations, this will not need to be defined.")
        @JsonProperty("created")
        @NotNull
        @Min(0L)
        Instant getCreated();

        @Schema(required = true, description = "The id of the entity that created this entity. For create operations, this will not need to be defined and will come from the larger security context.")
        @JsonProperty("creator")
        @NotNull
        @Size(min = 37, max = 37)
        UUID getCreator();

        @Schema(required = true, description = "The unique identifier for this entity. For create operations, this will not be defined.")
        @JsonProperty("id")
        @NotNull
        @Size(min = 37, max = 37)
        UUID getId();

        @Schema(required = true, description = "Display name of this entity.")
        @JsonProperty("name")
        @NotNull
        @Size(min = 0)
        String getName();

        @Schema(required = true, description = "The identifier for the schema of this entity.")
        @JsonProperty("schema")
        @NotNull
        @Size(min = 37, max = 37)
        UUID getSchema();
    }

    static interface MutableEntity extends ImmutableEntity {
        @Schema(example = "0", description = "When the entity was modified as a Unix timestamp.")
        @JsonProperty("modified")
        @Min(0L)
        Instant getModified();

        @Schema(description = "The id of the entity that modified this entity.")
        @JsonProperty("modifier")
        @Size(min = 37, max = 37)
        UUID getModifier();
    }

    @Schema(description = "A `Note` is an immutable text annotation with an associated MIME type. ")
    static interface Note extends MutableEntity {
        @Schema(description = "The textual data of the note.")
        @JsonProperty("data")
        String getData();

        @Schema(description = "MIME type of the text content.")
        @JsonProperty("mime")
        String getMime();
    }

    @Path("/fruits")
    static class FruitResource {
        @GET
        @Path("/{fid}/notes/{nid}")
        @APIResponses(value = {
                @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Note.class))),
                @APIResponse(responseCode = "404", description = "Not Found - The `Fruit` or `Note` could not be found.")
        })
        public Response getNote(@PathParam("fid") final String fid, @PathParam("nid") final String nid) {
            return Response.ok().entity("magic!").build();
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String hello() {
            return "hello";
        }
    }
}
