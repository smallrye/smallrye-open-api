package test.io.smallrye.openapi.runtime.scanner.resources;

import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
@Secured({ "roles:removal" })
@SecurityScheme(securitySchemeName = "oauth", type = SecuritySchemeType.OAUTH2)
public class GreetingDeleteController {

    // 1) Basic path var test
    @DeleteMapping("/greet/{id}")
    public void greet(@PathVariable(name = "id") String id) {
        // No op
    }

    // 2) ResponseEntity without a type specified
    @DeleteMapping("/greetWithResponse/{id}")
    @APIResponse(responseCode = "204", description = "No Content")
    public ResponseEntity<Void> greetWithResponse(@PathVariable(name = "id") String id) {
        return ResponseEntity.noContent().build();
    }

    // 3) ResponseEntity with a type specified (No JaxRS comparison)
    @DeleteMapping("/greetWithResponseTyped/{id}")
    public ResponseEntity<Void> greetWithResponseTyped(@PathVariable(name = "id") String id) {
        return ResponseEntity.noContent().build();
    }

    // 4) Multiple paths var test
    @DeleteMapping(value = { "/multipleGreet1/{id}", "/multipleGreet2/{id}" })
    public void multipleGreet(@PathVariable(name = "id") String id) {
        // No op
    }
}
