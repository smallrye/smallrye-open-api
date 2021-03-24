package test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public abstract class BaseUser {

    @Min(value = 10)
    @NotNull
    protected Integer id;

}
