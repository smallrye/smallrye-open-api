package test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public interface User {

    @Positive
    @Max(value = 9999)
    Integer getId();

}
