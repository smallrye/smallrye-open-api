package test.io.smallrye.openapi.runtime.scanner.resources;

/**
 * Vert.x
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingDeleteResource in the JAX-RS test
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
//@RestController
//@RequestMapping(value = "/greeting", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class GreetingDeleteController {

    // 1) Basic path var test
    //@DeleteMapping("/greet/{id}")
    //public void greet(@PathVariable(name = "id") String id) {

    //}

    // 2) ResponseEntity without a type specified
    //@DeleteMapping("/greetWithResponse/{id}")
    //@APIResponse(responseCode = "204", description = "No Content")
    //public ResponseEntity greetWithResponse(@PathVariable(name = "id") String id) {
    //    return ResponseEntity.noContent().build();
    //}

    // 3) ResponseEntity with a type specified (No JaxRS comparison)
    //@DeleteMapping("/greetWithResponseTyped/{id}")
    //public ResponseEntity<Void> greetWithResponseTyped(@PathVariable(name = "id") String id) {
    //   return ResponseEntity.noContent().build();
    //}
}
