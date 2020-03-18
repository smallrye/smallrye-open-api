package io.smallrye.openapi.runtime.io.schema;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.ExternalDocumentable;
import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to Schema
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#schemaObject">schemaObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SchemaConstant implements Referenceable, ExternalDocumentable {

    public static final DotName DOTNAME_SCHEMA = DotName.createSimple(Schema.class.getName());
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

    static final String PROP_MIN_PROPERTIES = "minProperties";
    static final String PROP_ALL_OF = "allOf";
    static final String PROP_MAX_ITEMS = "maxItems";
    static final String PROP_EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    static final String PROP_DEFAULT_VALUE = "defaultValue";
    static final String PROP_DEFAULT = "default";
    static final String PROP_DISCRIMINATOR_MAPPING = "discriminatorMapping";
    static final String PROP_ANY_OF = "anyOf";
    static final String PROP_SCHEMA = "schema";
    static final String PROP_ADDITIONAL_PROPERTIES = "additionalProperties";
    static final String PROP_MULTIPLE_OF = "multipleOf";
    static final String PROP_DEPRECATED = "deprecated";
    static final String PROP_MINIMUM = "minimum";
    static final String PROP_DISCRIMINATOR_PROPERTY = "discriminatorProperty";
    static final String PROP_MAXIMUM = "maximum";
    static final String PROP_READ_ONLY = "readOnly";
    static final String PROP_TITLE = "title";

    static final String PROP_NULLABLE = "nullable";
    static final String PROP_UNIQUE_ITEMS = "uniqueItems";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_MIN_LENGTH = "minLength";

    // for annotations (reserved words in Java)
    static final String PROP_ENUMERATION = "enumeration";
    static final String PROP_ENUM = "enum";
    static final String PROP_MAX_LENGTH = "maxLength";
    static final String PROP_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
    static final String PROP_WRITE_ONLY = "writeOnly";
    static final String PROP_VALUE = "value";
    static final String PROP_MIN_ITEMS = "minItems";
    static final String PROP_ONE_OF = "oneOf";
    static final String PROP_ITEMS = "items";
    static final String PROP_MAX_PROPERTIES = "maxProperties";

    // Only in SchemaFactory ?
    static final String PROP_REQUIRED_PROPERTIES = "requiredProperties";
    static final String PROP_PROPERTIES = "properties";
    static final String PROP_NOT = "not";

    private SchemaConstant() {
    }
}
