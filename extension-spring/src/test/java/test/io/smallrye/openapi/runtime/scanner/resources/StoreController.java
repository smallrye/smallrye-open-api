package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import test.io.smallrye.openapi.runtime.scanner.entities.Order;

@RestController
@RequestMapping("/v2")
public class StoreController {

    @RequestMapping(value = "/store/order/{orderId}", produces = { "application/xml",
            "application/json" }, method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteOrder(@Min(1L) @PathVariable("orderId") Long orderId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/store/inventory", produces = { "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Map<String, Integer>> getInventory() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/store/order/{orderId}", produces = { "application/xml",
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Order> getOrderById(@Min(1L) @Max(10L) @PathVariable("orderId") Long orderId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/store/order", produces = { "application/xml", "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Order> placeOrder(@Valid @RequestBody Order body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
