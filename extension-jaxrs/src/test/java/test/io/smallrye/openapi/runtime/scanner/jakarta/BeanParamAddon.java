package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.HeaderParam;

public interface BeanParamAddon {

    @HeaderParam(value = "hi1")
    void setHeaderParam1(String value);

}
