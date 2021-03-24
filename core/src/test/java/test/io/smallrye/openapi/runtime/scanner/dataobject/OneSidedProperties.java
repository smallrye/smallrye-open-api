package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class OneSidedProperties extends OneSidedParent {

    String prop1;
    String prop2;
    String prop3;

    @Schema(hidden = true)
    public String getProp1() {
        return prop1;
    }

    public void setProp1(String prop1) {
        this.prop1 = prop1;
    }

    public String getProp2() {
        return prop2;
    }

    @Schema(hidden = true)
    public void setProp2(String prop2) {
        this.prop2 = prop2;
    }

}
