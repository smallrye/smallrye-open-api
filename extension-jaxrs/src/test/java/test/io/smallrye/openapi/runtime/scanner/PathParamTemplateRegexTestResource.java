package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(value = "/template")
public class PathParamTemplateRegexTestResource {

    @GET
    @Path(value = "{id:\\d+}/{name: [A-Z]+    }/{  nickname :[a-zA-Z]+}/{age: [0-9]{1,3}}")
    @Produces(value = MediaType.TEXT_PLAIN)
    public String echo(@PathParam(value = "id") Integer id, @PathParam(value = "name") String name,
            @PathParam(value = "nickname") String nickname, @PathParam(value = "age") String age) {
        return String.valueOf(id) + ' ' + name + ' ' + nickname + ' ' + age;
    }

}
