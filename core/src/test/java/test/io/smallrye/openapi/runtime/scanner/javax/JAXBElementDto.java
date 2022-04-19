package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlType(name = "JAXBElementDto", propOrder = { "caseSubtitleFree", "caseSubtitle" })
public class JAXBElementDto {

    @XmlElementRef(name = "CaseSubtitle", namespace = "urn:Milo.API.Miljo.DataContracts.V1", type = JAXBElement.class, required = false)
    protected JAXBElement<String> caseSubtitle;
    @XmlElementRef(name = "CaseSubtitleFree", namespace = "urn:Milo.API.Miljo.DataContracts.V1", type = JAXBElement.class, required = false)
    protected JAXBElement<String> caseSubtitleFree;

}
