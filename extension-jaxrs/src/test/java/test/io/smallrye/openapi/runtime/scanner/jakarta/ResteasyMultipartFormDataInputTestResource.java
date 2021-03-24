package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path(value = "multipart-form-data-input")
public class ResteasyMultipartFormDataInputTestResource {

    @POST
    @Path(value = "post")
    @Consumes(value = MediaType.MULTIPART_FORM_DATA)
    @SuppressWarnings(value = "unused")
    public void post(MultipartFormDataInput input) {
    }

}
