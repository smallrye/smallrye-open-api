package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

@JsonbPropertyOrder(value = { "type", "lovesRocks" })
public class Lizard extends AbstractPet {

    boolean lovesRocks;

}
