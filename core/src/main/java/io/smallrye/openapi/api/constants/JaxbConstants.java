package io.smallrye.openapi.api.constants;

import org.jboss.jandex.DotName;

/**
 * Constants related to the JAXB Specification
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class JaxbConstants {

    public static final DotName XML_TYPE = DotName
            .createSimple("javax.xml.bind.annotation.XmlType");
    public static final DotName XML_ELEMENT = DotName
            .createSimple("javax.xml.bind.annotation.XmlElement");
    public static final DotName XML_ATTRIBUTE = DotName
            .createSimple("javax.xml.bind.annotation.XmlAttribute");
    public static final DotName XML_ACCESSOR_TYPE = DotName
            .createSimple("javax.xml.bind.annotation.XmlAccessorType");
    public static final DotName XML_TRANSIENT = DotName
            .createSimple("javax.xml.bind.annotation.XmlTransient");
    public static final DotName XML_ROOTELEMENT = DotName
            .createSimple("javax.xml.bind.annotation.XmlRootElement");
    public static final DotName XML_WRAPPERELEMENT = DotName
            .createSimple("javax.xml.bind.annotation.XmlElementWrapper");

    public static final String PROP_NAME = "name";

    private JaxbConstants() {
    }
}
