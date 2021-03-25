package test.io.smallrye.openapi.runtime.scanner;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

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
