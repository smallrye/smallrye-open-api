package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path(value = "/{pathParam1}/{pathParam2}")
public class ParameterRefTestResource {

    @GET
    @Path(value = "one")
    @Parameter(ref = "queryParam1")
    String exampleEndpoint1(@PathParam(value = "pathParam1") String pathParam1,
            @PathParam(value = "pathParam2") String pathParam2) {
        return null;
    }

    @GET
    @Path(value = "/two")
    @Parameter(name = "pathParam1", style = ParameterStyle.SIMPLE)
    @Parameter(ref = "pathParam2")
    @Parameter(ref = "queryParam1", name = "queryParamOne")
    @Parameter(in = ParameterIn.COOKIE, description = "Ignored: missing key attributes")
    @Parameter(in = ParameterIn.DEFAULT, description = "Ignored: missing key attributes")
    @Parameter(in = ParameterIn.HEADER, description = "Ignored: missing key attributes")
    @Parameter(in = ParameterIn.PATH, description = "Ignored: missing key attributes")
    String exampleEndpoint2(@PathParam(value = "pathParam1") String pathParam1,
            @Parameter(hidden = true) @PathParam(value = "pathParam2") String pathParam2) {
        return null;
    }

}
