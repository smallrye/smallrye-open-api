package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;

@Schema(description = "A `Note` is an immutable text annotation with an associated MIME type. ")
public interface Note extends MutableEntity {

    @Schema(description = "The textual data of the note.")
    @JsonProperty(value = "data")
    String getData();

    @Schema(description = "MIME type of the text content.")
    @JsonProperty(value = "mime")
    String getMime();

}
