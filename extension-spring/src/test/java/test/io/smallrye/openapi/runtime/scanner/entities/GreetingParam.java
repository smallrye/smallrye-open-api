package test.io.smallrye.openapi.runtime.scanner.entities;

import jakarta.ws.rs.QueryParam;

public class GreetingParam {
    @QueryParam("nameQuery") // "real" spring does not require this, but quarkus-spring-web does
    private final String name;

    public GreetingParam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}