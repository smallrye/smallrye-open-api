package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public interface ImmutableEntity {

    @Schema(example = "0", required = true, description = "When the entity was created as a Unix timestamp. For create operations, this will not need to be defined.")
    @JsonProperty(value = "created")
    @NotNull
    @Min(value = 0L)
    Instant getCreated();

    @Schema(required = true, description = "The id of the entity that created this entity. For create operations, this will not need to be defined and will come from the larger security context.")
    @JsonProperty(value = "creator")
    @NotNull
    @Size(min = 37, max = 37)
    UUID getCreator();

    @Schema(required = true, description = "The unique identifier for this entity. For create operations, this will not be defined.")
    @JsonProperty(value = "id")
    @NotNull
    @Size(min = 37, max = 37)
    UUID getId();

    @Schema(required = true, description = "Display name of this entity.")
    @JsonProperty(value = "name")
    @NotNull
    @Size(min = 0)
    String getName();

    @Schema(required = true, description = "The identifier for the schema of this entity.")
    @JsonProperty(value = "schema")
    @NotNull
    @Size(min = 37, max = 37)
    UUID getSchema();

}
