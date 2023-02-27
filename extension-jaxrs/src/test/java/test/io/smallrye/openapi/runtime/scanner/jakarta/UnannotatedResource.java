package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.Set;

import jakarta.ws.rs.GET;

public class UnannotatedResource {

    @GET
    public Set<String> list() {
        return null;
    }

}
