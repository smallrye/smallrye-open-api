package test.io.smallrye.openapi.runtime.scanner.dataobject.javax;

import javax.xml.bind.annotation.XmlTransient;

public class XmlTransientField {

    @XmlTransient
    String prop1Field;
    String prop2Field;

}
