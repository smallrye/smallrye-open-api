package test.io.smallrye.openapi.runtime.scanner.entities;

public class GreetingParam {
    private final String name;

    public GreetingParam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}