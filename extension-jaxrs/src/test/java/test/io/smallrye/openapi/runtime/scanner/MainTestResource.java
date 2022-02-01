package test.io.smallrye.openapi.runtime.scanner;

import java.time.LocalDateTime;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path(value = "/resource")
@SuppressWarnings(value = "unused")
public class MainTestResource {

    public MainTestResource(@MatrixParam(value = "r0m1") LocalDateTime m0) {
    }

    public void setSomethingElse(Long code) {
    }

    @MatrixParam(value = "r0m0")
    public void setM0(LocalDateTime m0) {
    }

    @Path(value = "/sub/unknown1")
    @Parameter(name = "u1q", in = ParameterIn.QUERY, style = ParameterStyle.SIMPLE, description = "Parameter to make a sub-resource locator look like a bean property param")
    public Object getUnknownResource1(Long code) {
        return null;
    }

    @Path(value = "/sub/unknown2")
    public Object getUnknownResource2(
            @Parameter(name = "u2q", in = ParameterIn.QUERY, style = ParameterStyle.SIMPLE, description = "Parameter to make a sub-resource locator look like a bean property param") Long code) {
        return null;
    }

    @Path(value = "/sub0")
    @GET
    @Parameter(name = "q4", description = "Q4 Query")
    public String getHello(@QueryParam(value = "q4") String q4) {
        return "hello";
    }

    @Path(value = "/sub/{id}")
    @Parameter(name = "id", description = "Resource Identifier")
    public Sub1TestResource<String> get(@PathParam(value = "id") String id, @QueryParam(value = "q1") String q1,
            @MatrixParam(value = "m1") String m1, @MatrixParam(value = "m2") int m2, @FormParam(value = "f1") String f1) {
        return null;
    }

}
