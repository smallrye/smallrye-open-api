package test.io.smallrye.openapi.runtime.scanner.dataobject;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class BVTestResourceEntity {

    @Size(min = 5, max = 100)
    @NotNull
    @Schema(minLength = 10, maxLength = 101, nullable = true, name = "string_no_bean_constraints", required = false)
    @JsonbProperty(value = "string_no_bean_constraints")
    private String stringIgnoreBvContraints;
    @Size(min = 1, max = 2000)
    @Digits(integer = 100, fraction = 100)
    @NotNull
    @Schema(minimum = "101", maximum = "101.999", nullable = true, pattern = "^\\d{1,3}([.]\\d{1,3})?$", name = "big_int_no_bean_constraints", required = false)
    @JsonbProperty(value = "big_int_no_bean_constraints")
    private BigInteger bIntegerIgnoreBvContraints;
    @NotNull
    @NotEmpty
    @Size(max = 200)
    @Schema(minItems = 0, maxItems = 100, nullable = true, name = "list_no_bean_constraints", required = false)
    @JsonbProperty(value = "list_no_bean_constraints")
    private List<String> listIgnoreBvContraints;
    @NotNull
    @NotEmpty
    @Size(max = 200)
    @Schema(minProperties = 0, maxProperties = 100, nullable = true, name = "map_no_bean_constraints", required = false)
    @JsonbProperty(value = "map_no_bean_constraints")
    private Map<String, String> mapIgnoreBvContraints;
    @NotNull
    TestEnum enumValue;

}
