package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
