package test.io.smallrye.openapi.runtime.scanner.javax;

import java.time.LocalDate;

import javax.ws.rs.Path;

import test.io.smallrye.openapi.runtime.scanner.javax.Greetable.GreetingBean;

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

}
