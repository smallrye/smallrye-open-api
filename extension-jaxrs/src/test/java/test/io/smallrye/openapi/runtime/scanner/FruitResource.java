package test.io.smallrye.openapi.runtime.scanner;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(value = "/fruits")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@SuppressWarnings(value = "unused")
public class FruitResource {

    @GET
    public Set<Fruit> list() {
        return null;
    }

    @POST
    public Set<Fruit> add(Fruit fruit) {
        return null;
    }

    @DELETE
    public Set<Fruit> delete(Fruit fruit) {
        return null;
    }

}
