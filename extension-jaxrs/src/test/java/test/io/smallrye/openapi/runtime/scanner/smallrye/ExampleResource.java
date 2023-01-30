package test.io.smallrye.openapi.runtime.scanner.smallrye;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.smallrye.openapi.api.annotations.SmallRyeOpenApiPath;
import test.io.smallrye.openapi.runtime.scanner.Fruit;

@SmallRyeOpenApiPath("/fruits")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class ExampleResource {

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    public List<Fruit> list() {
        return new ArrayList<>();
    }

    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    public Fruit add(Fruit fruit) {
        return fruit;
    }

    @DELETE
    public Response delete() {
        return Response.noContent().build();
    }
}
