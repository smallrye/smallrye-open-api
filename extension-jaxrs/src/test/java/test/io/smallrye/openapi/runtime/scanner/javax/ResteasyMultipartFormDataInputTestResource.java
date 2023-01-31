package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path(value = "multipart-form-data-input")
public class ResteasyMultipartFormDataInputTestResource {

    @POST
    @Path(value = "post")
    @Consumes(value = MediaType.MULTIPART_FORM_DATA)
    @SuppressWarnings(value = "unused")
    public void post(HttpHeaders headers, MultipartFormDataInput input) {
    }

}
