package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@SuppressWarnings(value = "unused")
public class Sub3TestResource {

    @GET
    @Path(value = "{subsubid}")
    public String getSub3(@PathParam("sub3-id") String sub3Id, @PathParam(value = "subsubid") String subsubid) {
        return "Sub3TestResource";
    }

}
