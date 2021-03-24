package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@SuppressWarnings(value = "unused")
public class Sub2TestResource<T> {

    @GET
    @Path(value = "{subsubid}")
    public T getSub2(@PathParam(value = "subsubid") String subsubid) {
        return null;
    }

}
