package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
