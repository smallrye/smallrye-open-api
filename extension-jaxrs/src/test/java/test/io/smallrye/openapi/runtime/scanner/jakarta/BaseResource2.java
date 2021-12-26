package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class BaseResource2<T, S> {

    @GET
    @Path(value = "typevar")
    public T test(@QueryParam(value = "q1") @Schema(description = "Description for q1's schema") S q1) {
        return null;
    }

    @GET
    @Path(value = "map")
    public Map<String, T> getMap() {
        return null;
    }

}
