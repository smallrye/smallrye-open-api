package test.io.smallrye.openapi.runtime.scanner;

import java.util.Optional;

import javax.validation.constraints.Size;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

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
