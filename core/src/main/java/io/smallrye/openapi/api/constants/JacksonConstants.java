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
    public static final DotName JSON_UNWRAPPED = DotName
            .createSimple("com.fasterxml.jackson.annotation.JsonUnwrapped");
    public static final DotName JSON_VALUE = DotName
            .createSimple("com.fasterxml.jackson.annotation.JsonValue");
    public static final DotName JSON_VIEW = DotName
            .createSimple("com.fasterxml.jackson.annotation.JsonView");
    public static final DotName JSON_NAMING = DotName
            .createSimple("com.fasterxml.jackson.databind.annotation.JsonNaming");
    public static final DotName ENUM_NAMING = DotName
            .createSimple("com.fasterxml.jackson.databind.annotation.EnumNaming");

    public static final String PROP_VALUE = "value";
    public static final String PROP_ACCESS = "access";

    public static final String PROP_ACCESS_AUTO = "AUTO";
    public static final String PROP_ACCESS_READ_ONLY = "READ_ONLY";
    public static final String PROP_ACCESS_WRITE_ONLY = "WRITE_ONLY";
    public static final String PROP_ACCESS_READ_WRITE = "READ_WRITE";

    private JacksonConstants() {
    }
}
