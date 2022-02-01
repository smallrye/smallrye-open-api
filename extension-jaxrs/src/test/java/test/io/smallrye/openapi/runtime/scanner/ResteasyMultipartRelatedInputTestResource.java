package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedInput;

@Path(value = "multipart-related-input")
public class ResteasyMultipartRelatedInputTestResource {

    @POST
    @Path(value = "post/{id}")
    @Consumes(value = "multipart/related")
    @RequestBody(required = true)
    @SuppressWarnings(value = "unused")
    public void post(@org.jboss.resteasy.annotations.jaxrs.PathParam(value = "id") String id, MultipartRelatedInput input) {
    }

}
