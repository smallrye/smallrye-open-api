package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@Path(value = "/parameters-in-constructor/{id}/{p1}")
@SuppressWarnings(value = "unused")
public class ParametersInConstructorTestResource {

    public static class Bean {

        @PathParam(value = "id")
        @DefaultValue(value = "BEAN")
        String id;
    }

    private Bean param;

    public ParametersInConstructorTestResource(
            @Parameter(name = "h1", in = ParameterIn.HEADER, description = "Description of h1") @HeaderParam(value = "h1") @Deprecated String h1,
            @Parameter(name = "h2", in = ParameterIn.HEADER, hidden = true) @HeaderParam(value = "h2") String h2,
            @Parameter(name = "q1", deprecated = true) @QueryParam(value = "q1") String q1,
            @NotNull @CookieParam(value = "c1") String c1, @PathParam(value = "p1") String p1, @BeanParam Bean b1) {
    }

    @DELETE
    public void deleteWidget() {
    }

}
