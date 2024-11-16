package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.security.RolesAllowed;

import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.entities.GreetingParam;

/**
 * Spring.
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingGetResource in the JAX-RS test
 *
 * Here we use RequestMapping and not GetMapping.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@RestController
@RequestMapping(value = "/greeting", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@SecurityScheme(securitySchemeName = "oauth", type = SecuritySchemeType.OAUTH2)
public class GreetingGetControllerAlt {

    // 1) Basic path var test
    @RequestMapping(value = "/helloPathVariable/{name}", method = RequestMethod.GET)
    public Greeting helloPathVariable(@PathVariable(name = "name") String name) {
        return new Greeting("Hello " + name);
    }

    // 2) Basic path var test
    @RequestMapping(value = "/hellosPathVariable/{name}", method = RequestMethod.GET)
    public List<Greeting> hellosPathVariable(@PathVariable(name = "name") String name) {
        return Arrays.asList(new Greeting("Hello " + name));
    }

    // 3) Basic path var with Optional test
    @RequestMapping(value = "/helloOptional/{name}", method = RequestMethod.GET)
    public Optional<Greeting> helloOptional(@PathVariable(name = "name") String name) {
        return Optional.of(new Greeting("Hello " + name));
    }

    // 4) Basic request param test
    @RequestMapping(value = "/helloRequestParam", method = RequestMethod.GET)
    @RolesAllowed({ "roles:retrieval-by-query" })
    public Greeting helloRequestParam(@RequestParam(value = "name", required = false) String name) {
        return new Greeting("Hello " + name);
    }

    // 4a) Basic request with parameter-object test
    @RequestMapping(value = "/helloParameterObject", method = RequestMethod.GET)
    public Greeting helloParameterObject(@ParameterObject() GreetingParam params) {
        return new Greeting("Hello " + params.getName());
    }

    // 5) ResponseEntity without a type specified
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/helloPathVariableWithResponse/{name}", method = RequestMethod.GET)
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public ResponseEntity<Greeting> helloPathVariableWithResponse(@PathVariable(name = "name") String name) {
        return ResponseEntity.ok(new Greeting("Hello " + name));
    }

    // 6) ResponseEntity with a type specified (No JaxRS comparison)
    @RequestMapping(value = "/helloPathVariableWithResponseTyped/{name}", method = RequestMethod.GET)
    public ResponseEntity<Greeting> helloPathVariableWithResponseTyped(@PathVariable(name = "name") String name) {
        return ResponseEntity.ok(new Greeting("Hello " + name));
    }

    // 7) Test override of produces
    @RequestMapping(value = "/overrideProduces/{name}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String overrideProduces(@PathVariable(name = "name") String name) {
        return "Hello " + name;
    }

}
