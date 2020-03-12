package test.io.smallrye.openapi.runtime.scanner.entities;

import java.util.List;

import org.eclipse.microprofile.openapi.apps.airlines.model.CreditCard;
import org.eclipse.microprofile.openapi.apps.airlines.model.Flight;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class SpecialCaseTestContainer {

    // Collection with concrete generic type.
    List<String> listOfString;

    // List of indexed object. NB: Do we remember to read this?
    List<CreditCard> ccList;

    // Wildcard with super bound
    List<? super Flight> listSuperFlight;

    // Wildcard with extends bound
    List<? extends Foo> listExtendsFoo;

    // Wildcard with no bound
    List<?> listOfAnything;
}
