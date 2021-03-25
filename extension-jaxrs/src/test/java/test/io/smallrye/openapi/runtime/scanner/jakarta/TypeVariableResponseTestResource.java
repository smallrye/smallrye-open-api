package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

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
