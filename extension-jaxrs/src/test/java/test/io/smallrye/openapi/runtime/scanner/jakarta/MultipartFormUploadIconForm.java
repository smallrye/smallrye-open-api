package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.FormParam;

public class MultipartFormUploadIconForm extends MultipartFormVerify {

    @FormParam(value = "icon")
    public byte[] icon;

}
