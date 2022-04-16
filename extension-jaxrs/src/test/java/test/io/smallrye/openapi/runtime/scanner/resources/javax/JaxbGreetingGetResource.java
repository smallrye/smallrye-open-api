package test.io.smallrye.openapi.runtime.scanner.resources.javax;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.entities.javax.JaxbGreeting;
import test.io.smallrye.openapi.runtime.scanner.entities.javax.JaxbWithNameGreeting;

/**
 * JAX-RS.
 * Some basic tests, mostly to compare with the Spring implementation
 *
 * @author Andrey Batalev (andrey.batalev@gmail.com)
 */
@Path("/greeting")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
public class JaxbGreetingGetResource {

    @GET
    @Path("/helloPathVariable1/{name}")
    public Greeting helloPathVariable1(@PathParam("name") String name) {
        return new Greeting("Hello " + name);
    }

    @GET
    @Path("/helloPathVariable2/{name}")
    public JaxbGreeting helloPathVariable2(@PathParam("name") String name) {
        return new JaxbGreeting("Hello " + name);
    }

    @GET
    @Path("/helloPathVariable3/{name}")
    public JaxbWithNameGreeting helloPathVariable3(@PathParam("name") String name) {
        return new JaxbWithNameGreeting("Hello " + name, "Title!", null);
    }

}
