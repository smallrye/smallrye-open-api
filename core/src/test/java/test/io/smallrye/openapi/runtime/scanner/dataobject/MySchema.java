package test.io.smallrye.openapi.runtime.scanner.dataobject;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

// Out of order on purpose
@JsonbPropertyOrder(value = { "field1", "field3", "field2" })
public interface MySchema {

    @Schema(required = true)
    String getField1();

    @Schema(name = "anotherField")
    String getField2();

    String getField3();

}
