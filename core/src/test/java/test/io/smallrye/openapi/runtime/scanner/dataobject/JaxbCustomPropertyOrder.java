package test.io.smallrye.openapi.runtime.scanner.dataobject;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "theName", "comment2ActuallyFirst", "comment", "name2" })
public class JaxbCustomPropertyOrder {

    @XmlElement(name = "theName")
    String name;
    @XmlAttribute
    String name2;
    @XmlElement
    String comment;
    @XmlAttribute(name = "comment2ActuallyFirst")
    String comment2;

    public String getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }

    public String getName2() {
        return name2;
    }

    public String getComment2() {
        return comment2;
    }

}
