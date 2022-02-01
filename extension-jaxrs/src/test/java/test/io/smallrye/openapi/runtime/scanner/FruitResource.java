package test.io.smallrye.openapi.runtime.scanner;

import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
