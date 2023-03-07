package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestMatrix;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import test.io.smallrye.openapi.runtime.scanner.Widget;

@Path(value = "/all/the/params/{id1}/{id2}")
@SuppressWarnings(value = "unused")
public class RestEasyReactiveAllTheParamsTestResource {

    public RestEasyReactiveAllTheParamsTestResource(@RestPath(value = "id1") int id1, @RestPath String id2) {
    }

    public static class Bean {

        @RestMatrix
        @DefaultValue(value = "BEAN1")
        String matrixF1;
        @RestMatrix(value = "matrixF2")
        @DefaultValue(value = "BEAN2")
        String matrixF2;
        @RestCookie
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
            @Parameter(style = ParameterStyle.FORM, explode = Explode.TRUE) @RestForm(value = "f1") @DefaultValue(value = "42") int f1,
            @RestForm @DefaultValue(value = "f2-default") @NotNull String f2, @RestHeader(value = "h1") @Deprecated int h1,
            @RestHeader(value = "h2") String notH2) {
        return null;
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public RestResponse<Widget> get(@RestQuery(value = "q1") @Deprecated long q1, @RestQuery(value = "q2") String notQ2) {
        return null;
    }

    @GET
    @Path("stream")
    @Produces(value = MediaType.APPLICATION_JSON)
    public RestResponse<Stream<Widget>> getAll(@RestQuery(value = "q1") @Deprecated long q1,
            @RestQuery(value = "q2") String notQ2) {
        return null;
    }
}
