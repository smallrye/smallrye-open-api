package test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(value = XmlAccessType.PROPERTY)
public class XmlAccessTypePropertyOnly {

    String prop2Field;

    public String getProp1Property() {
        return null;
    }

}
