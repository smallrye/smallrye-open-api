package io.smallrye.openapi.runtime.io.schema;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.jandex.DotName;

/**
 * Constants related to Schema
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#schemaObject">schemaObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SchemaConstant {

    public static final DotName DOTNAME_SCHEMA = DotName.createSimple(Schema.class.getName());
    public static final DotName DOTNAME_TRUE_SCHEMA = DotName.createSimple(Schema.class.getName() + "$True");
    public static final DotName DOTNAME_FALSE_SCHEMA = DotName.createSimple(Schema.class.getName() + "$False");
    public static final String PROP_DISCRIMINATOR = "discriminator";
    public static final String PROP_XML = "xml";
    public static final String PROP_NAME = "name";
    public static final String PROP_REQUIRED = "required";
    public static final String PROP_IMPLEMENTATION = "implementation";
    public static final String PROP_HIDDEN = "hidden";
    public static final String PROP_TYPE = "type";
    public static final String PROP_FORMAT = "format";
    public static final String PROP_PATTERN = "pattern";
    public static final String PROP_EXAMPLE = "example";
    public static final String PROP_EXTERNAL_DOCS = "externalDocs";

    public static final String PROP_MIN_PROPERTIES = "minProperties";
    public static final String PROP_ALL_OF = "allOf";
    public static final String PROP_MAX_ITEMS = "maxItems";
    public static final String PROP_EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    public static final String PROP_DEFAULT_VALUE = "defaultValue";
    public static final String PROP_DEFAULT = "default";
    public static final String PROP_DISCRIMINATOR_MAPPING = "discriminatorMapping";
    public static final String PROP_ANY_OF = "anyOf";
    public static final String PROP_SCHEMA = "schema";
    public static final String PROP_ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String PROP_MULTIPLE_OF = "multipleOf";
    public static final String PROP_DEPRECATED = "deprecated";
    public static final String PROP_MINIMUM = "minimum";
    public static final String PROP_DISCRIMINATOR_PROPERTY = "discriminatorProperty";
    public static final String PROP_MAXIMUM = "maximum";
    public static final String PROP_READ_ONLY = "readOnly";
    public static final String PROP_TITLE = "title";

    public static final String PROP_NULLABLE = "nullable";
    public static final String PROP_UNIQUE_ITEMS = "uniqueItems";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_MIN_LENGTH = "minLength";

    // for annotations (reserved words in Java)
    public static final String PROP_ENUMERATION = "enumeration";
    public static final String PROP_ENUM = "enum";
    public static final String PROP_MAX_LENGTH = "maxLength";
    public static final String PROP_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
    public static final String PROP_WRITE_ONLY = "writeOnly";
    public static final String PROP_VALUE = "value";
    public static final String PROP_MIN_ITEMS = "minItems";
    public static final String PROP_ONE_OF = "oneOf";
    public static final String PROP_ITEMS = "items";
    public static final String PROP_MAX_PROPERTIES = "maxProperties";

    // Only in SchemaFactory ?
    public static final String PROP_REQUIRED_PROPERTIES = "requiredProperties";
    public static final String PROP_PROPERTIES = "properties";
    public static final String PROP_NOT = "not";

    public static final List<String> PROPERTIES_NONDISPLAY = Collections.unmodifiableList(Arrays.asList(PROP_IMPLEMENTATION,
            PROP_NAME,
            PROP_REQUIRED));

    private SchemaConstant() {
    }
}
