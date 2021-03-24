package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class NonJavaBeanMutatorProperty {

    String name;

    String name() {
        return name;
    }

    // Should be skipped
    void anotherValue(String value) {
        return;
    }

    // Should be skipped
    String get() {
        return name;
    }

    // Should be skipped
    String isNotAnAccessor() {
        return null;
    }

    @Schema(title = "Name of the property")
    void name(String name) {
        this.name = name;
    }

}
