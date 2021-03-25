package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Encoding;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "/multipart/{id1}/{id2}")
@SuppressWarnings(value = "unused")
public class MultipartFormTestResource {

    public MultipartFormTestResource(@PathParam(value = "id1") int id1,
            @org.jboss.resteasy.annotations.jaxrs.PathParam String id2) {
    }

    public static class Bean {

        @org.jboss.resteasy.annotations.jaxrs.FormParam
        @DefaultValue(value = "f1-default")
        String formField1;
        @FormParam(value = "f2")
        @DefaultValue(value = "default2")
        @PartType(value = "text/plain")
        String formField2;
        @FormParam(value = "data")
        private InputPart data;
        @FormParam(value = "binaryData")
        @PartType(value = MediaType.APPLICATION_OCTET_STREAM)
        @Schema(type = SchemaType.STRING, format = "binary")
        private byte[] binaryData;
        @FormParam(value = "file")
        @PartType(value = MediaType.APPLICATION_OCTET_STREAM)
        @Schema(type = SchemaType.STRING, format = "binary")
        private InputStream file;
        @FormParam(value = "undocumentedFile")
        @PartType(value = MediaType.APPLICATION_OCTET_STREAM)
        @Schema(hidden = true)
        private InputStream undocumentedFile;
    }

    @POST
    @Consumes(value = MediaType.MULTIPART_FORM_DATA)
    @Produces(value = MediaType.APPLICATION_JSON)
    @RequestBody(content = @Content(schema = @Schema(requiredProperties = { "f3" }), encoding = {
            @Encoding(name = "formField1", contentType = "text/x-custom-type") }))
    public CompletableFuture<Widget> upd(@MultipartForm Bean form,
            @FormParam(value = "f3") @DefaultValue(value = "3") int formField3,
            @org.jboss.resteasy.annotations.jaxrs.FormParam @NotNull String formField4) {
        return null;
    }

}
