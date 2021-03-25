package io.smallrye.openapi.api.constants;

import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.DotName;

/**
 * Constants related to the JSON-B Specification
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class JsonbConstants {

    public static final List<DotName> JSONB_PROPERTY = Arrays.asList(
            DotName.createSimple("javax.json.bind.annotation.JsonbProperty"),
            DotName.createSimple("jakarta.json.bind.annotation.JsonbProperty"));

    public static final List<DotName> JSONB_TRANSIENT = Arrays.asList(
            DotName.createSimple("javax.json.bind.annotation.JsonbTransient"),
            DotName.createSimple("jakarta.json.bind.annotation.JsonbTransient"));

    public static final List<DotName> JSONB_PROPERTY_ORDER = Arrays.asList(
            DotName.createSimple("javax.json.bind.annotation.JsonbPropertyOrder"),
            DotName.createSimple("jakarta.json.bind.annotation.JsonbPropertyOrder"));

    public static final String PROP_VALUE = "value";

    private JsonbConstants() {
    }
}
