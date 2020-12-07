package test.io.smallrye.openapi.runtime.scanner.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MyGreeting")
public class JaxbWithNameGreeting {

    @XmlAttribute
    private final String message;

    public JaxbWithNameGreeting(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
