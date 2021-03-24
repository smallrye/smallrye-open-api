package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public interface Feline {

    @Schema(name = "name", required = false, example = "Feline")
    void setName(String name);

}
