package test.io.smallrye.openapi.runtime.scanner.dataobject;

@com.fasterxml.jackson.annotation.JsonPropertyOrder(value = { "theName", "comment2ActuallyFirst", "comment" })
public class JacksonPropertyOrderCustomName {

    @com.fasterxml.jackson.annotation.JsonProperty(value = "theName")
    String name;
    String name2;
    String comment;
    @com.fasterxml.jackson.annotation.JsonProperty(value = "comment2ActuallyFirst")
    String comment2;

    public String getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }

}
