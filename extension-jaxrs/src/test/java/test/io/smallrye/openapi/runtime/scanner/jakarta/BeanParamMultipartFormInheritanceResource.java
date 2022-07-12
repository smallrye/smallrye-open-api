package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(value = "/")
public class BeanParamMultipartFormInheritanceResource {

    @POST
    @Path(value = "/uploadIcon")
    @Consumes(value = MediaType.MULTIPART_FORM_DATA)
    public Response uploadUserAvatar(
            @org.jboss.resteasy.annotations.providers.multipart.MultipartForm MultipartFormUploadIconForm form) {
        return null;
    }

    @POST
    @Path(value = "/uploadIcon/reactive")
    @Consumes(value = MediaType.MULTIPART_FORM_DATA)
    public Response uploadUserAvatarReactive(@org.jboss.resteasy.reactive.MultipartForm MultipartFormUploadIconForm form) {
        return null;
    }

    @GET
    @Path(value = "/beanparambase")
    public Response getWithBeanParams(@BeanParam BeanParamBase params) {
        return null;
    }

    @GET
    @Path(value = "/beanparamimpl")
    public Response getWithBeanParams(@BeanParam BeanParamImpl params) {
        return null;
    }

}
