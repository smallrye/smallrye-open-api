package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.time.LocalDate;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

public interface Greetable extends Conversation {

    public static class GreetingBean {

        String name;

        @FormParam(value = "greetingName")
        public void setName(String name) {
            this.name = name;
        }
    }

    @PathParam(value = "from")
    @Parameter(name = "from", in = ParameterIn.PATH, description = "The name of the person sending the greeting")
    void setFromName(String from);

    @HeaderParam(value = "date")
    @Parameter(name = "date", in = ParameterIn.HEADER, description = "The local date when the greeting is sent", allowEmptyValue = true)
    void setGreetingDate(LocalDate date);

    @POST
    @Path(value = "/greet/{from}")
    @Consumes(value = MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(value = MediaType.TEXT_PLAIN)
    @Parameter(name = "greetingName")
    String greet(@BeanParam GreetingBean bean);

}
