package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path(value = "/variable-types")
@SuppressWarnings(value = "unused")
public class TypeVariableResponseTestResource<TEST extends TypeVariableResponseTestResource.Dto> {

    public static class Dto {

        String id;
    }

    @GET
    public List<TEST> getAll() {
        return null;
    }

    @GET
    @Path(value = "{id}")
    public TEST getOne(@PathParam(value = "id") String id) {
        return null;
    }

}
