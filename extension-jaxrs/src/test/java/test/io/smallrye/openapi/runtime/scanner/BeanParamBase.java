package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.QueryParam;

public class BeanParamBase implements BeanParamAddon {

    @QueryParam(value = "qc1")
    String qc1;

    @Override
    public void setHeaderParam1(String value) {
    }

}
