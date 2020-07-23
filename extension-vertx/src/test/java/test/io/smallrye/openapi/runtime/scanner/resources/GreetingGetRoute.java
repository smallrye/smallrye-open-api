package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpMethod;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

/**
 * Vert.x.
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingGetResource in the JAX-RS test
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
@RouteBase(path = "greeting", consumes = "application/json", produces = "application/json")
public class GreetingGetRoute {

    // 1) Basic path var test
    @Route(path = "/helloPathVariable/:name", methods = HttpMethod.GET)
    public Greeting helloPathVariable(@Param("name") String name) {
        return new Greeting("Hello " + name);
    }

    // 2) Basic path var test
    @Route(path = "/hellosPathVariable/:name", methods = HttpMethod.GET)
    public List<Greeting> hellosPathVariable(@Param("name") String name) {
        return Arrays.asList(new Greeting("Hello " + name));
    }

    // 3) Basic path var with Optional test
    @Route(path = "/helloOptional/:name", methods = HttpMethod.GET)
    public Optional<Greeting> helloOptional(@Param("name") String name) {
        return Optional.of(new Greeting("Hello " + name));
    }

    // 4) Basic request param test
    @Route(path = "/helloRequestParam", methods = HttpMethod.GET)
    public Greeting helloRequestParam(@Param("name") String name) {
        return new Greeting("Hello " + name);
    }

    // 5) Void, so without a type specified
    @Route(path = "/helloPathVariableWithResponse/:name", methods = HttpMethod.GET)
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public void helloPathVariableWithResponse(@Param("name") String name) {
        // 
    }
}
