package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.Set;

import javax.ws.rs.GET;

public class UnannotatedResource {

    @GET
    public Set<String> list() {
        return null;
    }

}
