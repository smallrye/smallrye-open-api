package io.smallrye.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/")
public class ExampleResource {

    @GET
    @Path("/hello")
    public String sayHello(@QueryParam("greeting") String greeting) {
        return greeting + " world!";
    }

}
