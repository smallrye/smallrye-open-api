package test.io.smallrye.openapi.runtime.scanner.resources;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.quarkus.vertx.web.RouteBase;
import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

/**
 * Vert.x.
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingPutResource in the JAX-RS test
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
@RouteBase(path = "greeting", consumes = "application/json", produces = "application/json")
public class GreetingPutRoute {

    // 1) Basic path var test
    @Route(path = "/greet/:id", methods = HttpMethod.PUT)
    public Greeting greet(@Body Greeting greeting, @Param("id") String id) {
        return greeting;
    }

    // 2) Void, so without a type specified
    @Route(path = "/greetWithResponse/:id", methods = HttpMethod.PUT)
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public void greetWithResponse(@Body Greeting greeting, @Param("id") String id) {
        //
    }
}
