package test.io.smallrye.openapi.runtime.scanner;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class Dog extends AbstractPet implements Canine {

    @Schema(name = "bark")
    String bark;

    @Override
    @Schema(name = "bark")
    public String getBark() {
        return bark;
    }

    @Schema(name = "dog_name", description = "An annotated method, no field!")
    public String getName() {
        return "Fido";
    }

    @Schema(description = "This property is not used due to being static")
    public static int getAge() {
        return -1;
    }

}
