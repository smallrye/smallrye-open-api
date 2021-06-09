package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import jakarta.ws.rs.Path;

@Path("/spanish")
public class SalutationSpanish implements Salutation {

    @Override
    public String get() {
        return "hola";
    }

}
