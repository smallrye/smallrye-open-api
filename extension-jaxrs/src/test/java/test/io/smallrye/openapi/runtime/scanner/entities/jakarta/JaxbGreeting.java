package test.io.smallrye.openapi.runtime.scanner.entities.jakarta;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JaxbGreeting {

    private final String message;

    public JaxbGreeting(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
