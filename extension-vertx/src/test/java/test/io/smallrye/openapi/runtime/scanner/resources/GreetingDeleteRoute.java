package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpMethod;

/**
 * Vert.x
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingDeleteResource in the JAX-RS test
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
@RouteBase(path = "greeting", consumes = "application/json", produces = "application/json")
public class GreetingDeleteRoute {

    // 1) Basic path var test
    @Route(path = "/greet/:id", methods = HttpMethod.DELETE)
    public void greet(@Param("id") String id) {

    }

}
