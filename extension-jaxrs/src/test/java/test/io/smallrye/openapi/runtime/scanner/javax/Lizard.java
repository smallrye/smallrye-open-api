package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.json.bind.annotation.JsonbPropertyOrder;

@JsonbPropertyOrder(value = { "type", "lovesRocks" })
public class Lizard extends AbstractPet {

    boolean lovesRocks;

}
