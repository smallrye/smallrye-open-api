package test.io.smallrye.openapi.runtime.scanner.entities;

import javax.json.bind.annotation.JsonbTransient;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */

public class JsonbTransientOnFieldExample {

    @JsonbTransient
    String ignoredField;

    String serializedField;
}
