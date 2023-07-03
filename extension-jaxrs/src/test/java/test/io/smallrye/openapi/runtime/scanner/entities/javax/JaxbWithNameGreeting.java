package test.io.smallrye.openapi.runtime.scanner.entities.javax;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MyGreeting")
@XmlAccessorType(XmlAccessType.NONE)
public class JaxbWithNameGreeting {

    @XmlAttribute
    private final String message;

    public JaxbWithNameGreeting(String message, String title, List<String> items) {
        this.message = message;
        this.title = title;
        this.books = items;
    }

    public String getMessage() {
        return message;
    }

    @XmlElementWrapper(name = "books-array")
    @XmlElement(name = "item")
    private final List<String> books;

    public List<String> getBooks() {
        return books;
    }

    @XmlElement(name = "xml-title")
    private String title;

    public String getTitle() {
        return title;
    }
}
