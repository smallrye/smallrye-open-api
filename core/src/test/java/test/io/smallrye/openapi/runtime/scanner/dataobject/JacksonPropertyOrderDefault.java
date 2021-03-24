package test.io.smallrye.openapi.runtime.scanner.dataobject;

@com.fasterxml.jackson.annotation.JsonPropertyOrder(value = { "comment", "name" })
public class JacksonPropertyOrderDefault {

    @com.fasterxml.jackson.annotation.JsonProperty(value = "theName")
    String name;
    String name2;
    String comment;
    String comment2;

    public String getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }

}
