package io.smallrye.openapi.api.constants;

import org.jboss.jandex.DotName;

/**
 * Constants related to the Jackson library
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class JacksonConstants {

    public static final DotName JSON_PROPERTY = DotName
            .createSimple("com.fasterxml.jackson.annotation.JsonProperty");
    public static final DotName JSON_IGNORE = DotName
            .createSimple("com.fasterxml.jackson.annotation.JsonIgnore");
    public static final DotName JSON_IGNORE_TYPE = DotName
            .createSimple("com.fasterxml.jackson.annotation.JsonIgnoreType");
    public static final DotName JSON_IGNORE_PROPERTIES = DotName
            .createSimple("com.fasterxml.jackson.annotation.JsonIgnoreProperties");
    public static final DotName JSON_PROPERTY_ORDER = DotName
            .createSimple("com.fasterxml.jackson.annotation.JsonPropertyOrder");

    public static final String PROP_VALUE = "value";

    private JacksonConstants() {
    }
}
