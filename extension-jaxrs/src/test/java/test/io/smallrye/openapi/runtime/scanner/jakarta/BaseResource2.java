package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

public class BaseResource2<T, S> {

    @GET
    @Path(value = "typevar")
    public T test(@QueryParam(value = "q1") S q1) {
        return null;
    }

    @GET
    @Path(value = "map")
    public Map<String, T> getMap() {
        return null;
    }

}
