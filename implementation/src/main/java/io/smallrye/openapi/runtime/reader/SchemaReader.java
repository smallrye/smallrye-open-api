package io.smallrye.openapi.runtime.reader;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.models.media.DiscriminatorImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.media.XMLImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.SchemaFactory;

/**
 * Reading the Schema annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#schemaObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SchemaReader {
    private static final Logger LOG = Logger.getLogger(SchemaReader.class);

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
        LOG.debug("Processing a map of @Schema annotations.");
        Map<String, Schema> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, MPOpenApiConstants.SCHEMA.PROP_NAME);

            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }

            /*
             * The name is REQUIRED when the schema is defined within
             * {@link org.eclipse.microprofile.openapi.annotations.Components}.
             */
            if (name != null) {
                map.put(name, SchemaFactory.readSchema(context.getIndex(), nested));
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
        LOG.debug("Processing a Schema from json.");
        String name = JsonUtil.stringProperty(node, MPOpenApiConstants.SCHEMA.PROP_NAME);

        Schema schema = new SchemaImpl(name);
        schema.setRef(JsonUtil.stringProperty(node, MPOpenApiConstants.SCHEMA.PROP_REF_VAR));
        schema.setFormat(JsonUtil.stringProperty(node, MPOpenApiConstants.SCHEMA.PROP_FORMAT));
        schema.setTitle(JsonUtil.stringProperty(node, MPOpenApiConstants.SCHEMA.PROP_TITLE));
        schema.setDescription(JsonUtil.stringProperty(node, MPOpenApiConstants.SCHEMA.PROP_DESCRIPTION));
        schema.setDefaultValue(readObject(node.get(MPOpenApiConstants.SCHEMA.PROP_DEFAULT)));
        schema.setMultipleOf(JsonUtil.bigDecimalProperty(node, MPOpenApiConstants.SCHEMA.PROP_MULTIPLE_OF));
        schema.setMaximum(JsonUtil.bigDecimalProperty(node, MPOpenApiConstants.SCHEMA.PROP_MAXIMUM));
        schema.setExclusiveMaximum(JsonUtil.booleanProperty(node, MPOpenApiConstants.SCHEMA.PROP_EXCLUSIVE_MAXIMUM));
        schema.setMinimum(JsonUtil.bigDecimalProperty(node, MPOpenApiConstants.SCHEMA.PROP_MINIMUM));
        schema.setExclusiveMinimum(JsonUtil.booleanProperty(node, MPOpenApiConstants.SCHEMA.PROP_EXCLUSIVE_MINIMUM));
        schema.setMaxLength(JsonUtil.intProperty(node, MPOpenApiConstants.SCHEMA.PROP_MAX_LENGTH));
        schema.setMinLength(JsonUtil.intProperty(node, MPOpenApiConstants.SCHEMA.PROP_MIN_LENGTH));
        schema.setPattern(JsonUtil.stringProperty(node, MPOpenApiConstants.SCHEMA.PROP_PATTERN));
        schema.setMaxItems(JsonUtil.intProperty(node, MPOpenApiConstants.SCHEMA.PROP_MAX_ITEMS));
        schema.setMinItems(JsonUtil.intProperty(node, MPOpenApiConstants.SCHEMA.PROP_MIN_ITEMS));
        schema.setUniqueItems(JsonUtil.booleanProperty(node, MPOpenApiConstants.SCHEMA.PROP_UNIQUE_ITEMS));
        schema.setMaxProperties(JsonUtil.intProperty(node, MPOpenApiConstants.SCHEMA.PROP_MAX_PROPERTIES));
        schema.setMinProperties(JsonUtil.intProperty(node, MPOpenApiConstants.SCHEMA.PROP_MIN_PROPERTIES));
        schema.setRequired(JsonUtil.readStringArray(node.get(MPOpenApiConstants.SCHEMA.PROP_REQUIRED)));
        schema.setEnumeration(JsonUtil.readObjectArray(node.get(MPOpenApiConstants.SCHEMA.PROP_ENUM)));
        schema.setType(readSchemaType(node.get(MPOpenApiConstants.SCHEMA.PROP_TYPE)));
        schema.setItems(readSchema(node.get(MPOpenApiConstants.SCHEMA.PROP_ITEMS)));
        schema.setNot(readSchema(node.get(MPOpenApiConstants.SCHEMA.PROP_NOT)));
        schema.setAllOf(readSchemaArray(node.get(MPOpenApiConstants.SCHEMA.PROP_ALL_OF)));
        schema.setProperties(readSchemas(node.get(MPOpenApiConstants.SCHEMA.PROP_PROPERTIES)));
        if (node.has(MPOpenApiConstants.SCHEMA.PROP_ADDITIONAL_PROPERTIES)
                && node.get(MPOpenApiConstants.SCHEMA.PROP_ADDITIONAL_PROPERTIES).isObject()) {
            schema.setAdditionalPropertiesSchema(readSchema(node.get(MPOpenApiConstants.SCHEMA.PROP_ADDITIONAL_PROPERTIES)));
        } else {
            schema.setAdditionalPropertiesBoolean(
                    JsonUtil.booleanProperty(node, MPOpenApiConstants.SCHEMA.PROP_ADDITIONAL_PROPERTIES));
        }
        schema.setReadOnly(JsonUtil.booleanProperty(node, MPOpenApiConstants.SCHEMA.PROP_READ_ONLY));
        schema.setXml(readXML(node.get(MPOpenApiConstants.SCHEMA.PROP_XML)));
        schema.setExternalDocs(ExternalDocsReader.readExternalDocs(node.get(MPOpenApiConstants.SCHEMA.PROP_EXTERNAL_DOCS)));
        schema.setExample(readObject(node.get(MPOpenApiConstants.SCHEMA.PROP_EXAMPLE)));
        schema.setOneOf(readSchemaArray(node.get(MPOpenApiConstants.SCHEMA.PROP_ONE_OF)));
        schema.setAnyOf(readSchemaArray(node.get(MPOpenApiConstants.SCHEMA.PROP_ANY_OF)));
        schema.setNot(readSchema(node.get(MPOpenApiConstants.SCHEMA.PROP_NOT)));
        schema.setDiscriminator(readDiscriminator(node.get(MPOpenApiConstants.SCHEMA.PROP_DISCRIMINATOR)));
        schema.setNullable(JsonUtil.booleanProperty(node, MPOpenApiConstants.SCHEMA.PROP_NULLABLE));
        schema.setWriteOnly(JsonUtil.booleanProperty(node, MPOpenApiConstants.SCHEMA.PROP_WRITE_ONLY));
        schema.setDeprecated(JsonUtil.booleanProperty(node, MPOpenApiConstants.SCHEMA.PROP_DEPRECATED));
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
        if (node == null || !node.isTextual()) {
            return null;
        }
        String strval = node.asText();
        return Schema.SchemaType.valueOf(strval.toUpperCase());
    }

    /**
     * Reads a list of schemas.
     * 
     * @param node the json array
     * @return List of Schema models
     */
    private static List<Schema> readSchemaArray(final JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        List<Schema> rval = new ArrayList<>(node.size());
        ArrayNode arrayNode = (ArrayNode) node;
        for (JsonNode arrayItem : arrayNode) {
            rval.add(readSchema(arrayItem));
        }
        return rval;
    }

    /**
     * Reads the {@link Schema} OpenAPI nodes.
     * 
     * @param node map of schema json nodes
     * @return Map of Schema model
     */
    public static Map<String, Schema> readSchemas(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Schema> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readSchema(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link XML} OpenAPI node.
     * 
     * @param node the json node
     * @return XML model
     */
    private static XML readXML(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        XML xml = new XMLImpl();
        xml.setName(JsonUtil.stringProperty(node, MPOpenApiConstants.XML.PROP_NAME));
        xml.setNamespace(JsonUtil.stringProperty(node, MPOpenApiConstants.XML.PROP_NAMESPACE));
        xml.setPrefix(JsonUtil.stringProperty(node, MPOpenApiConstants.XML.PROP_PREFIX));
        xml.setAttribute(JsonUtil.booleanProperty(node, MPOpenApiConstants.XML.PROP_ATTRIBUTE));
        xml.setWrapped(JsonUtil.booleanProperty(node, MPOpenApiConstants.XML.PROP_WRAPPED));
        ExtensionReader.readExtensions(node, xml);
        return xml;
    }

    /**
     * Reads a {@link Discriminator} OpenAPI node.
     * 
     * @param node the json node
     * @return Discriminator model
     */
    private static Discriminator readDiscriminator(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        Discriminator discriminator = new DiscriminatorImpl();
        discriminator.setPropertyName(JsonUtil.stringProperty(node, MPOpenApiConstants.DISCRIMINATOR.PROP_PROPERTY_NAME));
        discriminator.setMapping(JsonUtil.readStringMap(node.get(MPOpenApiConstants.DISCRIMINATOR.PROP_MAPPING)));
        return discriminator;
    }
}
