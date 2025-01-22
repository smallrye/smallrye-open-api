package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

    @Path(value = "/sub2/{sub2-id}")
    public Sub2TestResource<T> getSub2(@PathParam("sub2-id") String sub2Id) {
        return new Sub2TestResource<T>();
    }

    @Path(value = "/sub3/{sub3-id}")
    public Class<Sub3TestResource> getSub3(@PathParam("sub3-id") String sub3Id) {
        return Sub3TestResource.class;
    }
}
