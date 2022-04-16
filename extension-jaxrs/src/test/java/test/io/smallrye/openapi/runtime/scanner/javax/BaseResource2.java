package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

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
