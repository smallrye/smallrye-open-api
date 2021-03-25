package io.smallrye.openapi.api.constants;

import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.DotName;

/**
 * Constants related to the JAXB Specification
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class JaxbConstants {

    public static final List<DotName> JAXB_ELEMENT = Arrays.asList(
            DotName.createSimple("javax.xml.bind.JAXBElement"),
            DotName.createSimple("jakarta.xml.bind.JAXBElement"));
    public static final List<DotName> XML_TYPE = Arrays.asList(
            DotName.createSimple("javax.xml.bind.annotation.XmlType"),
            DotName.createSimple("jakarta.xml.bind.annotation.XmlType"));
    public static final List<DotName> XML_ELEMENT = Arrays.asList(
            DotName.createSimple("javax.xml.bind.annotation.XmlElement"),
            DotName.createSimple("jakarta.xml.bind.annotation.XmlElement"));
    public static final List<DotName> XML_ATTRIBUTE = Arrays.asList(
            DotName.createSimple("javax.xml.bind.annotation.XmlAttribute"),
            DotName.createSimple("jakarta.xml.bind.annotation.XmlAttribute"));
    public static final List<DotName> XML_ACCESSOR_TYPE = Arrays.asList(
            DotName.createSimple("javax.xml.bind.annotation.XmlAccessorType"),
            DotName.createSimple("jakarta.xml.bind.annotation.XmlAccessorType"));
    public static final List<DotName> XML_TRANSIENT = Arrays.asList(
            DotName.createSimple("javax.xml.bind.annotation.XmlTransient"),
            DotName.createSimple("jakarta.xml.bind.annotation.XmlTransient"));
    public static final List<DotName> XML_ROOTELEMENT = Arrays.asList(
            DotName.createSimple("javax.xml.bind.annotation.XmlRootElement"),
            DotName.createSimple("jakarta.xml.bind.annotation.XmlRootElement"));
    public static final List<DotName> XML_WRAPPERELEMENT = Arrays.asList(
            DotName.createSimple("javax.xml.bind.annotation.XmlElementWrapper"),
            DotName.createSimple("jakarta.xml.bind.annotation.XmlElementWrapper"));

    public static final String PROP_NAME = "name";

    private JaxbConstants() {
    }
}
