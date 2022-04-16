package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path(value = "/segments")
public class PathSegmentMatrixTestResource {

    @GET
    @Path(value = "seg1")
    @Produces(value = MediaType.TEXT_PLAIN)
    @Parameter(name = "segments", description = "Test", style = ParameterStyle.MATRIX, in = ParameterIn.PATH)
    public String echo(@PathParam(value = "segments") PathSegment segmentsMatrix) {
        return segmentsMatrix.getPath();
    }

}
