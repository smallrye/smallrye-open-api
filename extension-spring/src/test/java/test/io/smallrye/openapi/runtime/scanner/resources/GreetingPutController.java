package test.io.smallrye.openapi.runtime.scanner.resources;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

/**
 * Spring.
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingPutResource in the JAX-RS test
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@RestController
@RequestMapping(value = "/greeting", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class GreetingPutController {

    // 1) Basic path var test
    @PutMapping("/greet/{id}")
    public Greeting greet(@RequestBody Greeting greeting, @PathVariable(name = "id") String id) {
        return greeting;
    }

    // 2) ResponseEntity without a type specified
    @PutMapping("/greetWithResponse/{id}")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    public ResponseEntity<Greeting> greetWithResponse(@RequestBody Greeting greeting, @PathVariable(name = "id") String id) {
        return ResponseEntity.ok(greeting);
    }

    // 3) ResponseEntity with a type specified (No JaxRS comparison)
    @PutMapping("/greetWithResponseTyped/{id}")
    public ResponseEntity<Greeting> greetWithResponseTyped(@RequestBody Greeting greeting,
            @PathVariable(name = "id") String id) {
        return ResponseEntity.ok(greeting);
    }
}
