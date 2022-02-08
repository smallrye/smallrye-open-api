package test.io.smallrye.openapi.runtime.scanner.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@JsonIgnoreProperties({ "jsonIgnoreProperties1", "jsonIgnoreProperties2", "idHidden" })
public class JsonIgnorePropertiesOnClassExample extends JsonIgnorePropertiesParent {
    // Should be ignored by virtue of @JsonIgnoreProperties on class
    String jsonIgnoreProperties1;

    // Should be ignored by virtue of @JsonIgnoreProperties on class
    String jsonIgnoreProperties2;

    // Should be present
    String shouldBePresent;
}

class JsonIgnorePropertiesParent {
    String idHidden;

    String parentNamePresent;
}
