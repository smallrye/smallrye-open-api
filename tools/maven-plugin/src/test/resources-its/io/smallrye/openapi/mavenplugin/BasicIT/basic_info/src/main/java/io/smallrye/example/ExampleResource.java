package io.smallrye.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/")
public class ExampleResource {

    @GET
    @Path("/hello")
    public String sayHello(@QueryParam("greeting") String greeting) {
        return greeting + " world!";
    }

}
