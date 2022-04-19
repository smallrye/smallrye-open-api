package test.io.smallrye.openapi.runtime.scanner.dataobject.javax;

import javax.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import test.io.smallrye.openapi.runtime.scanner.dataobject.AbstractAnimal;
import test.io.smallrye.openapi.runtime.scanner.dataobject.Canine;

// "type" will be first due to ordering on AbstractAnimal
@javax.json.bind.annotation.JsonbPropertyOrder(value = { "name", "type", "bark" })
public class Dog extends AbstractAnimal implements Canine {

    @JsonbProperty(value = "bark")
    String bark;

    @Schema(name = "bark")
    public String getBark() {
        return bark;
    }

    @Override
    public String getName() {
        return "Fido";
    }

    @Schema(description = "This property is not used due to being static")
    public static int getStaticAge() {
        return -1;
    }

}
