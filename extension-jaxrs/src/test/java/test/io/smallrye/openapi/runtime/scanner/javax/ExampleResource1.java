package test.io.smallrye.openapi.runtime.scanner.javax;

import java.time.LocalDate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;

@Path(value = "/hi")
public class ExampleResource1 extends GenericResource implements Greetable {

    String from;

    @Override
    public void setFromName(String from) {
        this.from = from;
    }

    LocalDate date;

    @Override
    public void setGreetingDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String greet(GreetingBean bean) {
        return "hi " + bean.name + ", from: " + from + "; on date: " + date;
    }

    @Override
    @GET
    @Path(value = "/extension-alt")
    @Produces(value = MediaType.TEXT_PLAIN)
    @Operation(description = "example1 alternate extension")
    public String helloExtensionAlt() {
        return "hello example1";
    }
}
