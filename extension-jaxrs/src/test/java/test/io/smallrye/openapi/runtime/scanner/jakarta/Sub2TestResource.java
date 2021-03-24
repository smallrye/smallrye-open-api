package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@SuppressWarnings(value = "unused")
public class Sub2TestResource<T> {

    @GET
    @Path(value = "{subsubid}")
    public T getSub2(@PathParam(value = "subsubid") String subsubid) {
        return null;
    }

}
