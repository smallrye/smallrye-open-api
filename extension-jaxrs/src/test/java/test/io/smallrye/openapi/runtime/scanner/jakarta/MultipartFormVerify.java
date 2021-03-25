package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.FormParam;

public class MultipartFormVerify {

    @FormParam(value = "token")
    public String token;
    @FormParam(value = "os")
    public String os;

}
