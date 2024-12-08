package test.io.smallrye.openapi.runtime.scanner.resources;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;
import test.io.smallrye.openapi.runtime.scanner.entities.GreetingParam;

@RestController
@RequestMapping(value = "/greeting-with-params", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class GreetingParameterobjectController {

    @GetMapping("/hello")
    public Greeting hello(@ParameterObject() GreetingParam params) {
        return new Greeting("Hello " + params.getName());
    }

}
