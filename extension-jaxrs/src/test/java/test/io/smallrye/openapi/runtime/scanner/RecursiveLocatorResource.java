package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path(value = "/recursion")
@SuppressWarnings(value = "unused")
public class RecursiveLocatorResource {

    @GET
    @Path(value = "fetch")
    public String get() {
        return null;
    }

    @Path(value = "alternate1")
    public RecursiveLocatorResource getLocator1() {
        return this;
    }

    @Path(value = "alternate2")
    public RecursiveLocatorResource getLocator2() {
        return this;
    }

}
