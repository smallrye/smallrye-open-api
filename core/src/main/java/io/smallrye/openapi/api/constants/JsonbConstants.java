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

    /*
     * See jakarta.json.bind.config.PropertyNamingStrategy#IDENTITY
     */
    public static final String IDENTITY = "IDENTITY";

    /*
     * See jakarta.json.bind.config.PropertyNamingStrategy#LOWER_CASE_WITH_DASHES
     */
    public static final String LOWER_CASE_WITH_DASHES = "LOWER_CASE_WITH_DASHES";

    /*
     * See jakarta.json.bind.config.PropertyNamingStrategy#LOWER_CASE_WITH_UNDERSCORES
     */
    public static final String LOWER_CASE_WITH_UNDERSCORES = "LOWER_CASE_WITH_UNDERSCORES";

    /*
     * See jakarta.json.bind.config.PropertyNamingStrategy#UPPER_CAMEL_CASE
     */
    public static final String UPPER_CAMEL_CASE = "UPPER_CAMEL_CASE";

    /*
     * See jakarta.json.bind.config.PropertyNamingStrategy#UPPER_CAMEL_CASE_WITH_SPACES
     */
    public static final String UPPER_CAMEL_CASE_WITH_SPACES = "UPPER_CAMEL_CASE_WITH_SPACES";

    /*
     * See jakarta.json.bind.config.PropertyNamingStrategy#CASE_INSENSITIVE
     */
    public static final String CASE_INSENSITIVE = "CASE_INSENSITIVE";

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
