package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public interface Canine {

    @Schema(name = "c_name", description = "The name of the canine", maxLength = 50)
    public String getName();

}
