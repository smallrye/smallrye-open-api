package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.FormParam;

public class MultipartFormUploadIconForm extends MultipartFormVerify {

    @FormParam(value = "icon")
    public byte[] icon;

}
