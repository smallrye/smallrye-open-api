package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.quarkus.vertx.web.RouteBase;
import io.quarkus.vertx.web.RoutingExchange;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
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
    public Greeting helloPathVariable(RoutingContext context, @Param("name") String name) {
        return new Greeting("Hello " + name);
    }

    // 2) Basic path var test
    @Route(path = "/hellosPathVariable/:name", methods = HttpMethod.GET)
    public List<Greeting> hellosPathVariable(RoutingExchange routingExchange, @Param("name") String name) {
        return Arrays.asList(new Greeting("Hello " + name));
    }

    // 3) Basic path var with Optional test, name of parameter taken from argument name
    @Route(path = "/helloOptional/:name", methods = HttpMethod.GET)
    public Optional<Greeting> helloOptional(HttpServerRequest httpServerRequest, @Param String name) {
        return Optional.of(new Greeting("Hello " + name));
    }

    // 4) Basic request param test
    @Route(path = "/helloRequestParam", methods = HttpMethod.GET)
    public Greeting helloRequestParam(HttpServerResponse httpServerResponse,
            @Parameter(description = "The name") @Param("name") String name) {
        return new Greeting("Hello " + name);
    }

    // 5) Void, so without a type specified
    @Route(path = "/helloPathVariableWithResponse/:name", methods = HttpMethod.GET)
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public void helloPathVariableWithResponse(@Param("name") String name) {
        //
    }

    // 6) Failure should not end up the schema
    @Route(path = "/helloFailure/:name", methods = HttpMethod.GET, type = Route.HandlerType.FAILURE)
    public Greeting helloFailure(@Param("name") String name) {
        return new Greeting("Hello " + name);
    }

    // 7) Default GET
    @Route(path = "/defaultGet/:name")
    public Greeting helloDefault(@Param("name") String name) {
        return new Greeting("Hello " + name);
    }

    // 8) Default path
    @Route
    public Greeting helloPath(@Param("name") String name) {
        return new Greeting("Hello " + name);
    }

    // 9) Ignored due to use of `regex`
    @Route(regex = "\\/old-path")
    public Greeting helloRegex(RoutingContext context) {
        return new Greeting("Hello " + context.request().uri());
    }

    // 10) Included due to @Operation
    @Route(regex = "\\/complicated\\/path", methods = HttpMethod.GET)
    @Operation(operationId = "id-to-select-with-filter")
    public Greeting helloAgainRegex(RoutingContext context) {
        return new Greeting("Hello " + context.request().uri());
    }
}
