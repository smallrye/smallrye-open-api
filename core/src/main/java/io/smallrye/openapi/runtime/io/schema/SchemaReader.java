package io.smallrye.openapi.runtime.io.schema;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.discriminator.DiscriminatorReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsConstant;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsReader;
import io.smallrye.openapi.runtime.io.xml.XmlReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Schema annotation
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#schemaObject">schemaObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SchemaReader {

    private SchemaReader() {
    }

    /**
     * Reads a map of Schema annotations.
     * 
     * @param context the scanner context
     * @param annotationValue map of {@literal @}Schema annotations
     * @return Map of Schema models
     */
    public static Map<String, Schema> readSchemas(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.log.annotationsMap("@Schema");
        Map<String, Schema> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, SchemaConstant.PROP_NAME);

            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }

            /*
             * The name is REQUIRED when the schema is defined within
             * {@link org.eclipse.microprofile.openapi.annotations.Components}.
             */
            if (name != null) {
                map.put(name, SchemaFactory.readSchema(context.getIndex(), context.getClassLoader(), nested));
            } /*-
              //For consideration - be more lenient and attempt to use the name from the implementation's @Schema?
              else {
                if (JandexUtil.isSimpleClassSchema(nested)) {
                    Schema schema = SchemaFactory.readClassSchema(index, nested.value(OpenApiConstants.PROP_IMPLEMENTATION), false);
              
                    if (schema instanceof SchemaImpl) {
                        name = ((SchemaImpl) schema).getName();
              
                        if (name != null) {
                            map.put(name, schema);
                        }
                    }
                }
              }*/
        }
        return map;
    }

    /**
     * Reads a {@link Schema} OpenAPI node.
     * 
     * @param node json node
     * @return Schema model
     */
    public static Schema readSchema(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.log.singleJsonObject("Schema");
        String name = JsonUtil.stringProperty(node, SchemaConstant.PROP_NAME);

        Schema schema = new SchemaImpl(name);
        schema.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        schema.setFormat(JsonUtil.stringProperty(node, SchemaConstant.PROP_FORMAT));
        schema.setTitle(JsonUtil.stringProperty(node, SchemaConstant.PROP_TITLE));
        schema.setDescription(JsonUtil.stringProperty(node, SchemaConstant.PROP_DESCRIPTION));
        schema.setDefaultValue(readObject(node.get(SchemaConstant.PROP_DEFAULT)));
        schema.setMultipleOf(JsonUtil.bigDecimalProperty(node, SchemaConstant.PROP_MULTIPLE_OF));
        schema.setMaximum(JsonUtil.bigDecimalProperty(node, SchemaConstant.PROP_MAXIMUM));
        schema.setExclusiveMaximum(JsonUtil.booleanProperty(node, SchemaConstant.PROP_EXCLUSIVE_MAXIMUM).orElse(null));
        schema.setMinimum(JsonUtil.bigDecimalProperty(node, SchemaConstant.PROP_MINIMUM));
        schema.setExclusiveMinimum(JsonUtil.booleanProperty(node, SchemaConstant.PROP_EXCLUSIVE_MINIMUM).orElse(null));
        schema.setMaxLength(JsonUtil.intProperty(node, SchemaConstant.PROP_MAX_LENGTH));
        schema.setMinLength(JsonUtil.intProperty(node, SchemaConstant.PROP_MIN_LENGTH));
        schema.setPattern(JsonUtil.stringProperty(node, SchemaConstant.PROP_PATTERN));
        schema.setMaxItems(JsonUtil.intProperty(node, SchemaConstant.PROP_MAX_ITEMS));
        schema.setMinItems(JsonUtil.intProperty(node, SchemaConstant.PROP_MIN_ITEMS));
        schema.setUniqueItems(JsonUtil.booleanProperty(node, SchemaConstant.PROP_UNIQUE_ITEMS).orElse(null));
        schema.setMaxProperties(JsonUtil.intProperty(node, SchemaConstant.PROP_MAX_PROPERTIES));
        schema.setMinProperties(JsonUtil.intProperty(node, SchemaConstant.PROP_MIN_PROPERTIES));
        schema.setRequired(JsonUtil.readStringArray(node.get(SchemaConstant.PROP_REQUIRED)).orElse(null));
        schema.setEnumeration(JsonUtil.readObjectArray(node.get(SchemaConstant.PROP_ENUM)).orElse(null));
        schema.setType(readSchemaType(node.get(SchemaConstant.PROP_TYPE)));
        schema.setItems(readSchema(node.get(SchemaConstant.PROP_ITEMS)));
        schema.setNot(readSchema(node.get(SchemaConstant.PROP_NOT)));
        schema.setAllOf(readSchemaArray(node.get(SchemaConstant.PROP_ALL_OF)).orElse(null));
        schema.setProperties(readSchemas(node.get(SchemaConstant.PROP_PROPERTIES)).orElse(null));
        if (node.has(SchemaConstant.PROP_ADDITIONAL_PROPERTIES)
                && node.get(SchemaConstant.PROP_ADDITIONAL_PROPERTIES).isObject()) {
            schema.setAdditionalPropertiesSchema(readSchema(node.get(SchemaConstant.PROP_ADDITIONAL_PROPERTIES)));
        } else {
            schema.setAdditionalPropertiesBoolean(
                    JsonUtil.booleanProperty(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES).orElse(null));
        }
        schema.setReadOnly(JsonUtil.booleanProperty(node, SchemaConstant.PROP_READ_ONLY).orElse(null));
        schema.setXml(XmlReader.readXML(node.get(SchemaConstant.PROP_XML)));
        schema.setExternalDocs(ExternalDocsReader.readExternalDocs(node.get(ExternalDocsConstant.PROP_EXTERNAL_DOCS)));
        schema.setExample(readObject(node.get(SchemaConstant.PROP_EXAMPLE)));
        schema.setOneOf(readSchemaArray(node.get(SchemaConstant.PROP_ONE_OF)).orElse(null));
        schema.setAnyOf(readSchemaArray(node.get(SchemaConstant.PROP_ANY_OF)).orElse(null));
        schema.setNot(readSchema(node.get(SchemaConstant.PROP_NOT)));
        schema.setDiscriminator(DiscriminatorReader.readDiscriminator(node.get(SchemaConstant.PROP_DISCRIMINATOR)));
        schema.setNullable(JsonUtil.booleanProperty(node, SchemaConstant.PROP_NULLABLE).orElse(null));
        schema.setWriteOnly(JsonUtil.booleanProperty(node, SchemaConstant.PROP_WRITE_ONLY).orElse(null));
        schema.setDeprecated(JsonUtil.booleanProperty(node, SchemaConstant.PROP_DEPRECATED).orElse(null));
        ExtensionReader.readExtensions(node, schema);
        return schema;
    }

    /**
     * Reads a schema type.
     * 
     * @param node the json node
     * @return SchemaType enum
     */
    private static Schema.SchemaType readSchemaType(final JsonNode node) {
        if (node != null && node.isTextual()) {
            String strval = node.asText();
            return Schema.SchemaType.valueOf(strval.toUpperCase());
        }
        return null;
    }

    /**
     * Reads a list of schemas.
     * 
     * @param node the json array
     * @return List of Schema models
     */
    private static Optional<List<Schema>> readSchemaArray(final JsonNode node) {
        if (node != null && node.isArray()) {
            List<Schema> rval = new ArrayList<>(node.size());
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode arrayItem : arrayNode) {
                rval.add(readSchema(arrayItem));
            }
            return Optional.of(rval);
        }
        return Optional.empty();
    }

    /**
     * Reads the {@link Schema} OpenAPI nodes.
     * 
     * @param node map of schema json nodes
     * @return Map of Schema model
     */
    public static Optional<Map<String, Schema>> readSchemas(final JsonNode node) {
        if (node != null && node.isObject()) {
            Map<String, Schema> models = new LinkedHashMap<>();
            for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
                String fieldName = fieldNames.next();
                JsonNode childNode = node.get(fieldName);
                models.put(fieldName, readSchema(childNode));
            }
            return Optional.of(models);
        }
        return Optional.empty();
    }
}
