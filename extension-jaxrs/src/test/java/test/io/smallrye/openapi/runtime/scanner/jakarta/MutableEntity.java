package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public interface MutableEntity extends ImmutableEntity {

    @Schema(example = "0", description = "When the entity was modified as a Unix timestamp.")
    @JsonProperty(value = "modified")
    @Min(value = 0L)
    Instant getModified();

    @Schema(description = "The id of the entity that modified this entity.")
    @JsonProperty(value = "modifier")
    @Size(min = 37, max = 37)
    UUID getModifier();

}
