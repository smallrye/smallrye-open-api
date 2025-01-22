package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@SuppressWarnings(value = "unused")
public class Sub3TestResource {

    @GET
    @Path(value = "{subsubid}")
    public String getSub3(@PathParam("sub3-id") String sub3Id, @PathParam(value = "subsubid") String subsubid) {
        return "Sub3TestResource";
    }

}
