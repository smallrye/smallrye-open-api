package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

@Path(value = "/")
public class BeanParamMultipartFormInheritanceResource {

    @POST
    @Path(value = "/uploadIcon")
    @Consumes(value = MediaType.MULTIPART_FORM_DATA)
    public Response uploadUserAvatar(@MultipartForm MultipartFormUploadIconForm form) {
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
