package test.io.smallrye.openapi.runtime.scanner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */

public class JsonIgnoreOnFieldExample {
    // Should be ignored by virtue of @JsonIgnore
    @JsonIgnore
    String jsonIgnoreThisField;

    String thisFieldShouldAppear;
}
