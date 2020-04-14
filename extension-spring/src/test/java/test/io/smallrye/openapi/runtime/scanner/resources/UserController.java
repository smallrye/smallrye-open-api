package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import test.io.smallrye.openapi.runtime.scanner.entities.User;

@RestController
@RequestMapping("/v2")
public class UserController {

    @RequestMapping(value = "/user", produces = { "application/xml", "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Void> createUser(@Valid @RequestBody User body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/user/createWithArray", produces = { "application/xml",
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Void> createUsersWithArrayInput(@Valid @RequestBody List<User> body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/user/createWithList", produces = { "application/xml",
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Void> createUsersWithListInput(@Valid @RequestBody List<User> body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/user/{username}", produces = { "application/xml",
            "application/json" }, method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteUser(@PathVariable("username") String username) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/user/{username}", produces = { "application/xml",
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<User> getUserByName(@PathVariable("username") String username) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/user/login", produces = { "application/xml", "application/json" }, method = RequestMethod.GET)
    ResponseEntity<String> loginUser(@NotNull @Valid @RequestParam(value = "username", required = true) String username,
            @NotNull @Valid @RequestParam(value = "password", required = true) String password) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/user/logout", produces = { "application/xml", "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Void> logoutUser() {

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/user/{username}", produces = { "application/xml",
            "application/json" }, method = RequestMethod.PUT)
    ResponseEntity<Void> updateUser(@PathVariable("username") String username, @Valid @RequestBody User body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
