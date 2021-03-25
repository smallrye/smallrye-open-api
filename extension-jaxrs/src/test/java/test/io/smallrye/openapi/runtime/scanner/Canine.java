package test.io.smallrye.openapi.runtime.scanner;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public interface Canine {

    @Schema(name = "bark1", readOnly = true)
    public String getBark();

}
