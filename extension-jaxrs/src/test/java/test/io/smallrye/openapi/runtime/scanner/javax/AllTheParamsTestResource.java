package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.concurrent.CompletionStage;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "/all/the/params/{id1}/{id2}")
@SuppressWarnings(value = "unused")
public class AllTheParamsTestResource {

    public AllTheParamsTestResource(@PathParam(value = "id1") int id1,
            @org.jboss.resteasy.annotations.jaxrs.PathParam String id2) {
    }

    public static class Bean {

        @org.jboss.resteasy.annotations.jaxrs.MatrixParam
        @DefaultValue(value = "BEAN1")
        String matrixF1;
        @MatrixParam(value = "matrixF2")
        @DefaultValue(value = "BEAN2")
        String matrixF2;
        @org.jboss.resteasy.annotations.jaxrs.CookieParam
        @DefaultValue(value = "COOKIE1")
        @Deprecated
        String cookieF1;
    }

    @Parameter(in = ParameterIn.PATH, style = ParameterStyle.MATRIX, name = "id2")
    @BeanParam
    private Bean param;

    @POST
    @Consumes(value = MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(value = MediaType.APPLICATION_JSON)
    public CompletionStage<Widget> upd(
            @Parameter(style = ParameterStyle.FORM, explode = Explode.TRUE) @FormParam(value = "f1") @DefaultValue(value = "42") int f1,
            @org.jboss.resteasy.annotations.jaxrs.FormParam @DefaultValue(value = "f2-default") @NotNull String f2,
            @HeaderParam(value = "h1") @Deprecated int h1,
            @org.jboss.resteasy.annotations.jaxrs.HeaderParam(value = "h2") String notH2) {
        return null;
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public Widget get(@QueryParam(value = "q1") @Deprecated long q1,
            @org.jboss.resteasy.annotations.jaxrs.QueryParam(value = "q2") String notQ2) {
        return null;
    }

}
