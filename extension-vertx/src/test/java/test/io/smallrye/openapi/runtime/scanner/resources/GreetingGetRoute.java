package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.enterprise.context.ApplicationScoped;

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
    //@GetMapping("/hellosPathVariable/{name}")
    //public List<Greeting> hellosPathVariable(@PathVariable(name = "name") String name) {
    //   return Arrays.asList(new Greeting("Hello " + name));
    //}

    // 3) Basic path var with Optional test
    //@GetMapping("/helloOptional/{name}")
    //public Optional<Greeting> helloOptional(@PathVariable(name = "name") String name) {
    //    return Optional.of(new Greeting("Hello " + name));
    //}

    // 4) Basic request param test
    //@GetMapping("/helloRequestParam")
    //public Greeting helloRequestParam(@RequestParam(value = "name", required = false) String name) {
    //    return new Greeting("Hello " + name);
    //}

    // 5) ResponseEntity without a type specified
    //@GetMapping("/helloPathVariableWithResponse/{name}")
    //@APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    //public ResponseEntity helloPathVariableWithResponse(@PathVariable(name = "name") String name) {
    //    return ResponseEntity.ok(new Greeting("Hello " + name));
    //}

    // 6) ResponseEntity with a type specified (No JaxRS comparison)
    //@GetMapping("/helloPathVariableWithResponseTyped/{name}")
    //public ResponseEntity<Greeting> helloPathVariableWithResponseTyped(@PathVariable(name = "name") String name) {
    //    return ResponseEntity.ok(new Greeting("Hello " + name));
    //}
}
