package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
