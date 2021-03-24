package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.FormParam;

public class MultipartFormVerify {

    @FormParam(value = "token")
    public String token;
    @FormParam(value = "os")
    public String os;

}
