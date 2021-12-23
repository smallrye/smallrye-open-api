package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path(value = "/enum/formparam")
public class EnumQueryParamTestResource {

    public static enum TestEnum {
        VAL1,
        VAL2,
        VAL3
    }

    @Schema(name = "RestrictedEnum", title = "Restricted enum with fewer values", enumeration = { "VAL1",
            "VAL3" }, externalDocs = @ExternalDocumentation(description = "When to use RestrictedEnum?", url = "http://example.com/RestrictedEnum/info.html"))
    public static enum TestEnumWithSchema {
        VAL1,
        VAL2,
        VAL3
    }

    @SuppressWarnings(value = "unused")
    @POST
    @Consumes(value = MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(value = MediaType.TEXT_PLAIN)
    public TestEnum postData(@QueryParam(value = "val") TestEnum value, @QueryParam(value = "restr") TestEnumWithSchema restr) {
        return null;
    }

}
