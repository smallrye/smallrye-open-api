package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class OneSidedParent {

    @Schema(hidden = true)
    public String getParentProp1() {
        return "";
    }

    @Schema(hidden = true)
    public void setParentProp2(String something) {
    }

}
