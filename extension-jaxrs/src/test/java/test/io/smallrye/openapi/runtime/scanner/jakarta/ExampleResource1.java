package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.time.LocalDate;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
