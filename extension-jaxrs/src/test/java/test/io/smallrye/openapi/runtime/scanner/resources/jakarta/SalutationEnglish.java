package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import jakarta.ws.rs.Path;

@Path("/english")
public class SalutationEnglish implements Salutation {

    @Override
    public String get() {
        return "hello";
    }

}
