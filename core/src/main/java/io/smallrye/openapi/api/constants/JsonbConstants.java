package io.smallrye.openapi.api.constants;

import org.jboss.jandex.DotName;

/**
 * Constants related to the JSON-B Specification
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class JsonbConstants {

    public static final DotName JSONB_PROPERTY = DotName
            .createSimple("javax.json.bind.annotation.JsonbProperty");
    public static final DotName JSONB_TRANSIENT = DotName
            .createSimple("javax.json.bind.annotation.JsonbTransient");
    public static final DotName JSONB_PROPERTY_ORDER = DotName
            .createSimple("javax.json.bind.annotation.JsonbPropertyOrder");

    public static final String PROP_VALUE = "value";

    private JsonbConstants() {
    }
}
