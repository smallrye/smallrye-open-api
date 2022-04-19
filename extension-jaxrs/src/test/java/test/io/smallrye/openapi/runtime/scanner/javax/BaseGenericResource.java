package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

public class BaseGenericResource<T1, T2, T3> {

    public static class GenericBean<G> {

        @QueryParam(value = "g1")
        G g1;
    }

    @GET
    @Path(value = "typevar")
    public T1 test(@QueryParam(value = "q1") T2 q1) {
        return null;
    }

    @GET
    @Path(value = "map")
    public Map<T2, T3> getMap(T1 filter) {
        return null;
    }

}
