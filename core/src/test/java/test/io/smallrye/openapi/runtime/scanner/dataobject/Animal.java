package test.io.smallrye.openapi.runtime.scanner.dataobject;

public interface Animal {

    @com.fasterxml.jackson.annotation.JsonProperty
    default String speciesName() {
        return "Unknown";
    }

}
