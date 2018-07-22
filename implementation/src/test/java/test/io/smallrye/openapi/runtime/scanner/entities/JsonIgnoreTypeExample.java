package test.io.smallrye.openapi.runtime.scanner.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class JsonIgnoreTypeExample {
    // Should always be ignored by virtue of @JsonIgnoreType annotation
    IgnoreThisType shouldBeIgnoredType;
    IgnoreThisType shouldBeIgnoredType2;
    int shouldBePresent;

    @JsonIgnoreType
    private static final class IgnoreThisType {
        int foo;
    }
}
