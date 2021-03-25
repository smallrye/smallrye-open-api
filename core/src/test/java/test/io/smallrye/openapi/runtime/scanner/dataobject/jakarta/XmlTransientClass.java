package test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta;

import jakarta.xml.bind.annotation.XmlTransient;

@XmlTransient
public class XmlTransientClass {

    String prop1Field;
    String prop2Field;

    public String getProp3Property() {
        return null;
    }

}
