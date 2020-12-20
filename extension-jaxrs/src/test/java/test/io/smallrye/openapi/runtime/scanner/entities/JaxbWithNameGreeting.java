package test.io.smallrye.openapi.runtime.scanner.entities;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MyGreeting")
public class JaxbWithNameGreeting {

    @XmlAttribute
    private final String message;

    public JaxbWithNameGreeting(String message, List<String> items) {
        this.message = message;
        this.books = items;
    }

    public String getMessage() {
        return message;
    }

    @XmlElementWrapper(name = "books-array")
    private final List<String> books;

    public List<String> getBooks() {
        return books;
    }

}
