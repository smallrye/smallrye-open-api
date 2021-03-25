package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path(value = "/char/sequence")
public class CharSequenceArrayParamTestResource {

    public static class EchoResult {

        // The 'implementation' specifies one less array dimensions since the type implies one dimension
        @Schema(type = SchemaType.ARRAY, implementation = CharSequence[][].class)
        CharSequence[][][] resultWithSchema;
        CharSequence[][][] resultNoSchema;

        EchoResult result(CharSequence[][][] result) {
            this.resultWithSchema = result;
            this.resultNoSchema = result;
            return this;
        }
    }

    @GET
    @Produces(value = MediaType.TEXT_PLAIN)
    public EchoResult echo(@QueryParam(value = "val") CharSequence[][][] value) {
        return new EchoResult().result(value);
    }

}
