package test.io.smallrye.openapi.runtime.scanner.entities;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "Schema31Changes")
public class Schema31Changes {

    @Schema(nullable = true)
    private String nullableString;

    @Schema(minimum = "4", maximum = "10", exclusiveMinimum = true, exclusiveMaximum = true)
    private int rangedInt;

    public Schema31Changes(String nullableString, int rangedInt) {
        this.nullableString = nullableString;
        this.rangedInt = rangedInt;
    }

    public String getNullableString() {
        return nullableString;
    }

    public int getRangedInt() {
        return rangedInt;
    }
}
