package test.io.smallrye.openapi.runtime.scanner.dataobject.javax;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;

public interface User {

    @Positive
    @Max(value = 9999)
    Integer getId();

}
