package test.io.smallrye.openapi.runtime.scanner.resources;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

/**
 * Spring.
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingPostResource in the JAX-RS test
 * 
 * This is an alternative that use the RequestMapping rather than PostMapping
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@RestController
@RequestMapping(value = "/greeting", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class GreetingPostControllerAlt {

    // 1) Basic path var test
    @RequestMapping(value = "/greet", method = RequestMethod.POST)
    public Greeting greet(@RequestBody Greeting greeting) {
        return greeting;
    }

    // 2) ResponseEntity without a type specified
    @RequestMapping(value = "/greetWithResponse", method = RequestMethod.POST)
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public ResponseEntity greetWithResponse(@RequestBody Greeting greeting) {
        return ResponseEntity.ok(greeting);
    }

    // 3) ResponseEntity with a type specified (No JaxRS comparison)
    @RequestMapping(value = "/greetWithResponseTyped", method = RequestMethod.POST)
    public ResponseEntity<Greeting> greetWithResponseTyped(@RequestBody Greeting greeting) {
        return ResponseEntity.ok(greeting);
    }
}
