package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("foo")
public class FooResource {

    public static class Foo {
        private String name;
        private Bar bar;

        //... getters/setters
    }

    public static class Bar {
        private String note;

        //...getter/setter
    }

    @GET
    public Foo getFoo() {
        return new Foo();
    }
}