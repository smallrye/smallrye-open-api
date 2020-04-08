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

    // This is here so that IDEs see that the "foo" variable is used somewhere.
    public static final void main(String[] args) {
        IgnoreThisType type = new IgnoreThisType();
        type.foo = 17;
        System.out.println(type.foo);
    }
}
