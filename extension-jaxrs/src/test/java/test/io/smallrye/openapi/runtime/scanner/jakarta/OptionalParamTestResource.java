package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path(value = "/optional")
public class OptionalParamTestResource {

    public static class OptionalWrapper {

        Optional<String> value;
    }

    public static class NestedBean {

        @NotNull
        Optional<String> title;
    }

    @Schema(name = "multipurpose-bean")
    public static class Bean {

        @QueryParam(value = "name6")
        Optional<String> name;
        @QueryParam(value = "age6")
        OptionalDouble age;
        Optional<NestedBean> nested;
    }

    @GET
    @Path(value = "/n1")
    @Produces(value = MediaType.TEXT_PLAIN)
    public String helloName(@QueryParam(value = "name") Optional<String> name) {
        return "hello " + name.orElse("SmallRye!");
    }

    @GET
    @Path(value = "/n2")
    @Produces(value = MediaType.TEXT_PLAIN)
    @Parameter(name = "name2", required = true)
    public String helloName2(@QueryParam(value = "name2") Optional<String> name) {
        return "hello " + name.orElse("SmallRye!");
    }

    @GET
    @Path(value = "/n3")
    @Produces(value = MediaType.TEXT_PLAIN)
    @Parameter(name = "name3", required = false)
    public Optional<String> helloName3(@QueryParam(value = "name3") Optional<String> name) {
        return Optional.of("hello " + name.orElse("SmallRye!"));
    }

    @POST
    @Path(value = "/n4")
    @Consumes(value = MediaType.TEXT_PLAIN)
    @Produces(value = MediaType.TEXT_PLAIN)
    @Parameter(name = "name4")
    public OptionalWrapper helloName4(@FormParam(value = "name4") Optional<String> name) {
        OptionalWrapper r = new OptionalWrapper();
        r.value = Optional.of("hello " + name.orElse("SmallRye!"));
        return r;
    }

    @POST
    @Path(value = "/n5")
    @Consumes(value = MediaType.TEXT_PLAIN)
    @Produces(value = MediaType.TEXT_PLAIN)
    public Optional<String> helloName5(Optional<String> name, @CookieParam(value = "age5") OptionalLong age) {
        return Optional.of("hello " + name.orElse("SmallRye!") + ' ' + age.orElse(-1));
    }

    @SuppressWarnings(value = "unused")
    @GET
    @Path(value = "/n6")
    @Produces(value = MediaType.TEXT_PLAIN)
    public Optional<String> helloName6(@BeanParam Optional<Bean> bean) {
        return null;
    }

    @GET
    @Path(value = "/n7/{name}")
    @Produces(value = MediaType.TEXT_PLAIN)
    public String helloName7(@PathParam(value = "name") Optional<String> name) {
        return "hello " + name.orElse("SmallRye!");
    }

    @SuppressWarnings(value = "unused")
    @POST
    @Path(value = "/n8")
    @Consumes(value = MediaType.TEXT_PLAIN)
    @Produces(value = MediaType.TEXT_PLAIN)
    public Optional<String> helloName8(@RequestBody Optional<Bean> bean) {
        return null;
    }

    @GET
    @Path(value = "/n9")
    @Produces(value = MediaType.TEXT_PLAIN)
    public String helloName9(@QueryParam(value = "name9") @NotNull Optional<String> name) {
        return "hello " + name.orElse("SmallRye!");
    }

}
