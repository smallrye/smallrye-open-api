package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path(value = "multipart-mixed")
public class ResteasyMultipartInputTestResource {

    @POST
    @Path(value = "post")
    @Consumes(value = "multipart/mixed")
    @SuppressWarnings(value = "unused")
    public void post(MultipartInput input) {
    }

}
