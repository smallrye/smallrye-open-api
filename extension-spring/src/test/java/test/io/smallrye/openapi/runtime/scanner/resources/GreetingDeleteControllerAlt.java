package test.io.smallrye.openapi.runtime.scanner.resources;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring.
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingDeleteResource in the JAX-RS test
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@RestController
@RequestMapping(value = "/greeting", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class GreetingDeleteControllerAlt {

    // 1) Basic path var test
    @RequestMapping(value = "/greet/{id}", method = RequestMethod.DELETE)
    public void greet(@PathVariable(name = "id") String id) {

    }

    // 2) ResponseEntity without a type specified
    @RequestMapping(value = "/greetWithResponse/{id}", method = RequestMethod.DELETE)
    @APIResponse(responseCode = "204", description = "No Content")
    public ResponseEntity greetWithResponse(@PathVariable(name = "id") String id) {
        return ResponseEntity.noContent().build();
    }

    // 3) ResponseEntity with a type specified (No JaxRS comparison)
    @RequestMapping(value = "/greetWithResponseTyped/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> greetWithResponseTyped(@PathVariable(name = "id") String id) {
        return ResponseEntity.noContent().build();
    }
}
