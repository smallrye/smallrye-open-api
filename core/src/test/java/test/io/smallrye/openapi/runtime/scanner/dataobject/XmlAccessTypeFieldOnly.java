package test.io.smallrye.openapi.runtime.scanner.dataobject;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(value = XmlAccessType.FIELD)
public class XmlAccessTypeFieldOnly {

    String prop1Field;

    public String getProp2Property() {
        return null;
    }

}
