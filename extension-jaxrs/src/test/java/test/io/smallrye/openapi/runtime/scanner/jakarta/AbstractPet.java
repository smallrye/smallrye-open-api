package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/* Test models and resources below. */
public abstract class AbstractPet {

    @Schema(name = "pet_type", required = true)
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
