package test.io.smallrye.openapi.runtime.scanner;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@SuppressWarnings(value = "unused")
public class Sub1TestResource<T> {

    @QueryParam(value = "q2")
    T q2;

    @GET
    public String get(@QueryParam(value = "q3") String q3) {
        return null;
    }

    @PATCH
    @Consumes(value = MediaType.TEXT_PLAIN)
    public void update(String value) {
        return;
    }

    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    public void create(Map<String, CharSequence> attributes) {
        return;
    }

    @Path(value = "/sub2")
    public Sub2TestResource<T> getSub2() {
        return new Sub2TestResource<T>();
    }

}
