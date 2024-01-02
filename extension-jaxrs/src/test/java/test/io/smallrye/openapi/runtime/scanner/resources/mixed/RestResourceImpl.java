package test.io.smallrye.openapi.runtime.scanner.resources.mixed;

public class RestResourceImpl implements RestInterface {

    @Override
    public String getPublicResponse() {
        return "response value";
    }
}
