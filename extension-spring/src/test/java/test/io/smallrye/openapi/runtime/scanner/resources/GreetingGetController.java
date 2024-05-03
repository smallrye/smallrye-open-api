package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.entities.GreetingParam;

/**
 * Spring.
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingGetResource in the JAX-RS test
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@RestController
@RequestMapping(value = "/greeting", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@SecurityScheme(securitySchemeName = "oauth", type = SecuritySchemeType.OAUTH2)
public class GreetingGetController {

    // 1) Basic path var test
    @GetMapping("/helloPathVariable/{name}")
    public Greeting helloPathVariable(@PathVariable(name = "name") String name) {
        return new Greeting("Hello " + name);
    }

    // 2) Basic path var test
    @GetMapping(path = "/hellosPathVariable/{name}")
    public List<Greeting> hellosPathVariable(@PathVariable(name = "name") String name) {
        return Arrays.asList(new Greeting("Hello " + name));
    }

    // 3) Basic path var with Optional test
    @GetMapping("/helloOptional/{name}")
    public Optional<Greeting> helloOptional(@PathVariable(name = "name") String name) {
        return Optional.of(new Greeting("Hello " + name));
    }

    // 4) Basic request param test
    @GetMapping(path = "/helloRequestParam")
    @Secured({ "roles:retrieval-by-query" })
    public Greeting helloRequestParam(@RequestParam(value = "name", required = false) String name) {
        return new Greeting("Hello " + name);
    }

    // 4a) Basic request with parameter-object test
    @GetMapping("/helloParameterObject")
    public Greeting helloParameterObject(@ParameterObject() GreetingParam params) {
        return new Greeting("Hello " + params.getName());
    }

    // 5) ResponseEntity without a type specified
    @SuppressWarnings("rawtypes")
    @GetMapping("/helloPathVariableWithResponse/{name}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public ResponseEntity helloPathVariableWithResponse(@PathVariable(name = "name") String name) {
        return ResponseEntity.ok(new Greeting("Hello " + name));
    }

    // 6) ResponseEntity with a type specified (No JaxRS comparison)
    @GetMapping(path = "/helloPathVariableWithResponseTyped/{name}")
    public ResponseEntity<Greeting> helloPathVariableWithResponseTyped(@PathVariable(name = "name") String name) {
        return ResponseEntity.ok(new Greeting("Hello " + name));
    }

    // 7) Test override of produces
    @GetMapping(value = "/overrideProduces/{name}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String overrideProduces(@PathVariable(name = "name") String name) {
        return "Hello " + name;
    }

}
