package io.smallrye.openapi.runtime.io.schema;

import static io.smallrye.openapi.model.DataType.listOf;
import static io.smallrye.openapi.model.DataType.mapOf;
import static io.smallrye.openapi.model.DataType.type;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.model.DataType;
import io.smallrye.openapi.runtime.io.ReferenceIO;

/**
 * Constants related to Schema
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md#schema-object">schema-object</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SchemaConstant {

    public static final DotName DOTNAME_SCHEMA = DotName
            .createSimple(org.eclipse.microprofile.openapi.annotations.media.Schema.class.getName());
    public static final DotName DOTNAME_TRUE_SCHEMA = DotName
            .createSimple(org.eclipse.microprofile.openapi.annotations.media.Schema.class.getName() + "$True");
    public static final DotName DOTNAME_FALSE_SCHEMA = DotName
            .createSimple(org.eclipse.microprofile.openapi.annotations.media.Schema.class.getName() + "$False");
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
    public static final String PROP_EXAMPLES = "examples";
    public static final String PROP_EXTERNAL_DOCS = "externalDocs";
    public static final String REF = "ref";

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

    public static final String PROP_SCHEMA_DIALECT = "$schema";
    public static final String PROP_COMMENT = "$comment";
    public static final String PROP_REF = ReferenceIO.REF;

    public static final String PROP_IF = "if";
    public static final String PROP_THEN = "then";
    public static final String PROP_ELSE = "else";
    public static final String PROP_DEPENDENT_SCHEMAS = "dependentSchemas";
    public static final String PROP_PROPERTY_NAMES = "propertyNames";
    public static final String PROP_UNEVALUATED_ITEMS = "unevaluatedItems";
    public static final String PROP_UNEVALUATED_PROPERTIES = "unevaluatedProperties";

    public static final String PROP_CONST = "const";
    public static final String PROP_MAX_CONTAINS = "maxContains";
    public static final String PROP_MIN_CONTAINS = "minContains";
    public static final String PROP_DEPENDENT_REQUIRED = "dependentRequired";

    public static final String PROP_CONTENT_ENCODING = "contentEncoding";
    public static final String PROP_CONTENT_MEDIA_TYPE = "contentMediaType";
    public static final String PROP_CONTENT_SCHEMA = "contentSchema";

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
    public static final String PROP_PREFIX_ITEMS = "prefixItems";
    public static final String PROP_CONTAINS = "contains";
    public static final String PROP_PATTERN_PROPERTIES = "patternProperties";
    public static final String PROP_IF_SCHEMA = "ifSchema";
    public static final String PROP_THEN_SCHEMA = "thenSchema";
    public static final String PROP_ELSE_SCHEMA = "elseSchema";
    public static final String PROP_CONST_VALUE = "constValue";
    public static final String PROP_COMMENT_FIELD = "comment";

    // Only in SchemaFactory ?
    public static final String PROP_REQUIRED_PROPERTIES = "requiredProperties";
    public static final String PROP_PROPERTIES = "properties";
    public static final String PROP_NOT = "not";
    public static final String PROP_REGEX = "regex";
    public static final String PROP_REQUIRES = "requires";

    public static final String DIALECT_OAS31 = "https://spec.openapis.org/oas/3.1/dialect/base";
    public static final String DIALECT_JSON_2020_12 = "https://json-schema.org/draft/2020-12/schema";

    public static final List<String> PROPERTIES_NONDISPLAY = Collections.unmodifiableList(Arrays.asList(PROP_IMPLEMENTATION,
            PROP_NAME,
            PROP_REQUIRED));

    /**
     * A map of fields we expect to find in a Schema JSON object and their expected types
     */
    public static final Map<String, DataType> PROPERTIES_DATA_TYPES;
    public static final Map<String, DataType> PROPERTIES_DATA_TYPES_3_0;
    static {
        Map<String, DataType> propertiesDataTypes30 = new HashMap<>();
        Map<String, DataType> propertiesDataTypes = new HashMap<>();

        propertiesDataTypes30.put(SchemaConstant.PROP_DISCRIMINATOR, type(Discriminator.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_TITLE, type(String.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_DEFAULT, type(Object.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_ENUM, listOf(type(Object.class)));
        propertiesDataTypes30.put(SchemaConstant.PROP_MULTIPLE_OF, type(BigDecimal.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_MAX_LENGTH, type(Integer.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_MIN_LENGTH, type(Integer.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_PATTERN, type(String.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_MAX_ITEMS, type(Integer.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_MIN_ITEMS, type(Integer.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_UNIQUE_ITEMS, type(Boolean.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_MAX_PROPERTIES, type(Integer.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_MIN_PROPERTIES, type(Integer.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_REQUIRED, listOf(type(String.class)));
        propertiesDataTypes30.put(SchemaConstant.PROP_NOT, type(Schema.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_PROPERTIES, mapOf(type(Schema.class)));
        propertiesDataTypes30.put(SchemaConstant.PROP_ADDITIONAL_PROPERTIES, type(Schema.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_DESCRIPTION, type(String.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_FORMAT, type(String.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_READ_ONLY, type(Boolean.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_WRITE_ONLY, type(Boolean.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_EXAMPLE, type(Object.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_EXTERNAL_DOCS, type(ExternalDocumentation.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_DEPRECATED, type(Boolean.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_XML, type(XML.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_ITEMS, type(Schema.class));
        propertiesDataTypes30.put(SchemaConstant.PROP_ALL_OF, listOf(type(Schema.class)));
        propertiesDataTypes30.put(SchemaConstant.PROP_ANY_OF, listOf(type(Schema.class)));
        propertiesDataTypes30.put(SchemaConstant.PROP_ONE_OF, listOf(type(Schema.class)));

        propertiesDataTypes.putAll(propertiesDataTypes30);
        propertiesDataTypes.put(SchemaConstant.PROP_MAXIMUM, type(BigDecimal.class));
        propertiesDataTypes.put(SchemaConstant.PROP_EXCLUSIVE_MAXIMUM, type(BigDecimal.class));
        propertiesDataTypes.put(SchemaConstant.PROP_MINIMUM, type(BigDecimal.class));
        propertiesDataTypes.put(SchemaConstant.PROP_EXCLUSIVE_MINIMUM, type(BigDecimal.class));
        propertiesDataTypes.put(SchemaConstant.PROP_SCHEMA_DIALECT, type(String.class));
        propertiesDataTypes.put(SchemaConstant.PROP_COMMENT, type(String.class));
        propertiesDataTypes.put(SchemaConstant.PROP_IF, type(Schema.class));
        propertiesDataTypes.put(SchemaConstant.PROP_THEN, type(Schema.class));
        propertiesDataTypes.put(SchemaConstant.PROP_ELSE, type(Schema.class));
        propertiesDataTypes.put(SchemaConstant.PROP_DEPENDENT_SCHEMAS, mapOf(type(Schema.class)));
        propertiesDataTypes.put(SchemaConstant.PROP_PREFIX_ITEMS, listOf(type(Schema.class)));
        propertiesDataTypes.put(SchemaConstant.PROP_CONTAINS, type(Schema.class));
        propertiesDataTypes.put(SchemaConstant.PROP_PATTERN_PROPERTIES, mapOf(type(Schema.class)));
        propertiesDataTypes.put(SchemaConstant.PROP_PROPERTY_NAMES, type(Schema.class));
        propertiesDataTypes.put(SchemaConstant.PROP_UNEVALUATED_ITEMS, type(Schema.class));
        propertiesDataTypes.put(SchemaConstant.PROP_UNEVALUATED_PROPERTIES, type(Schema.class));
        propertiesDataTypes.put(SchemaConstant.PROP_CONST, type(Object.class));
        propertiesDataTypes.put(SchemaConstant.PROP_MAX_CONTAINS, type(Integer.class));
        propertiesDataTypes.put(SchemaConstant.PROP_MIN_CONTAINS, type(Integer.class));
        propertiesDataTypes.put(SchemaConstant.PROP_DEPENDENT_REQUIRED, mapOf(listOf(type(String.class))));
        propertiesDataTypes.put(SchemaConstant.PROP_CONTENT_ENCODING, type(String.class));
        propertiesDataTypes.put(SchemaConstant.PROP_CONTENT_MEDIA_TYPE, type(String.class));
        propertiesDataTypes.put(SchemaConstant.PROP_CONTENT_SCHEMA, type(Schema.class));
        propertiesDataTypes.put(SchemaConstant.PROP_EXAMPLES, listOf(type(Object.class)));

        PROPERTIES_DATA_TYPES_3_0 = Collections.unmodifiableMap(propertiesDataTypes30);
        PROPERTIES_DATA_TYPES = Collections.unmodifiableMap(propertiesDataTypes);
    }

    private SchemaConstant() {
    }
}
