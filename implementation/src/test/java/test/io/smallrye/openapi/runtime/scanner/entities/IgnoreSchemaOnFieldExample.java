package test.io.smallrye.openapi.runtime.scanner.entities;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */

public class IgnoreSchemaOnFieldExample {
    @Schema(hidden = true)
    String ignoredField;

    @Schema(hidden = false, description = "This field is not hidden")
    String serializedField1;

    @Schema(description = "This field is not hidden either")
    String serializedField2;

    String serializedField3;

}
