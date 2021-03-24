package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class NonJavaBeanAccessorProperty {

    String name;

    @Schema(title = "Name of the property")
    String name() {
        return name;
    }

    // Should be skipped
    String anotherValue() {
        return null;
    }

    // Should be skipped
    String get() {
        return name;
    }

    // Should be skipped
    String isNotAnAccessor() {
        return null;
    }

    void name(String name) {
        this.name = name;
    }

}
