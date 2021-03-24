package test.io.smallrye.openapi.runtime.scanner.dataobject;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public abstract class BaseUser {

    @Min(value = 10)
    @NotNull
    protected Integer id;

}
