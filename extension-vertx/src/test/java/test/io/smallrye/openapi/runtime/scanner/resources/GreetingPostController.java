package test.io.smallrye.openapi.runtime.scanner.resources;

/**
 * Vert.x.
 * Some basic test, comparing with what we get in the JAX-RS version.
 * See the GreetingPostResource in the JAX-RS test
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
//@RestController
//@RequestMapping(value = "/greeting", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class GreetingPostController {

    // 1) Basic path var test
    //@PostMapping("/greet")
    //public Greeting greet(@RequestBody Greeting greeting) {
    //    return greeting;
    //}

    // 2) ResponseEntity without a type specified
    //@PostMapping("/greetWithResponse")
    //@APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(ref = "#/components/schemas/Greeting")))
    //public ResponseEntity greetWithResponse(@RequestBody Greeting greeting) {
    //    return ResponseEntity.ok(greeting);
    //}

    // 3) ResponseEntity with a type specified (No JaxRS comparison)
    //@PostMapping("/greetWithResponseTyped")
    //public ResponseEntity<Greeting> greetWithResponseTyped(@RequestBody Greeting greeting) {
    //    return ResponseEntity.ok(greeting);
    //}
}
