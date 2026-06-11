package test.io.smallrye.openapi.runtime.scanner.resources;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import test.io.smallrye.openapi.runtime.scanner.entities.Greeting;

@Service
@RequestMapping(value = "/restless-greetings")
public class GreetingGetControllerNoRestController {

    @RequestMapping(value = "/howdy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Greeting> hello(HttpServletRequest request_,
            HttpServletResponse response_,
            Principal principal_) {
        return new ResponseEntity<>(new Greeting("Howdy!"), HttpStatus.OK);
    }

}
