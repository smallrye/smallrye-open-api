package test.io.smallrye.openapi.runtime.scanner;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

@JsonbPropertyOrder(value = { "type", "lovesRocks" })
public class Lizard extends AbstractPet {

    boolean lovesRocks;

}
