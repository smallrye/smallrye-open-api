package test.io.smallrye.openapi.runtime.scanner.dataobject;

import jakarta.xml.bind.annotation.XmlTransient;

public class XmlTransientField {

    @XmlTransient
    String prop1Field;
    String prop2Field;

}
