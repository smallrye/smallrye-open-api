package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path(value = "/enum-default-param")
public class DefaultEnumTestResource {

    public enum MyEnum {
        CAT,
        DOG,
        BAR,
        FOO
    }

    @GET
    @Produces(value = MediaType.TEXT_PLAIN)
    public String hello(@QueryParam(value = "q0") String q0,
            @Parameter(required = true) @QueryParam(value = "q1") @Size(min = 3, max = 3) @DefaultValue(value = "DOG") Optional<MyEnum> q1) {
        return "myEnum = " + q1;
    }

}
