package test.io.smallrye.openapi.runtime.scanner.dataobject.javax;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import test.io.smallrye.openapi.runtime.scanner.dataobject.AbstractAnimal;
import test.io.smallrye.openapi.runtime.scanner.dataobject.Feline;

// "type" will be first due to ordering on AbstractAnimal
@javax.xml.bind.annotation.XmlType(propOrder = { "name", "type" })
public class Cat extends AbstractAnimal implements Feline {

    @Schema(required = true, example = "Felix")
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @Schema(name = "type", required = false, example = "Cat")
    public String getType() {
        return super.getType();
    }

}
