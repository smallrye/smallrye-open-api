package test.io.smallrye.openapi.runtime.scanner.javax;

import java.time.LocalDate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import test.io.smallrye.openapi.runtime.scanner.javax.Greetable.GreetingBean;

@Path(value = "/hello")
public class ExampleResource2 extends GenericResource implements Greetable {

    @GET
    @Produces(value = MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    String from;

    @Override
    @Parameter(name = "from", in = ParameterIn.PATH, style = ParameterStyle.SIMPLE)
    public void setFromName(String from) {
        this.from = from;
    }

    @Parameter(name = "date", in = ParameterIn.HEADER, example = "2019-12-31", allowEmptyValue = false)
    LocalDate date;

    @Override
    public void setGreetingDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String greet(GreetingBean bean) {
        return "hello " + bean.name + ", from: " + from + "; on date: " + date;
    }

}
