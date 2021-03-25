package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlType;

@Schema
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlType(name = "JAXBElementDto", propOrder = { "caseSubtitleFree", "caseSubtitle" })
public class JAXBElementDto {

    @XmlElementRef(name = "CaseSubtitle", namespace = "urn:Milo.API.Miljo.DataContracts.V1", type = JAXBElement.class, required = false)
    protected JAXBElement<String> caseSubtitle;
    @XmlElementRef(name = "CaseSubtitleFree", namespace = "urn:Milo.API.Miljo.DataContracts.V1", type = JAXBElement.class, required = false)
    protected JAXBElement<String> caseSubtitleFree;

}
