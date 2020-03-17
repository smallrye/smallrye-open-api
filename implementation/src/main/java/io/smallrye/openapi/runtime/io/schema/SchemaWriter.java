package io.smallrye.openapi.runtime.io.schema;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Schema;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.components.ComponentsConstant;
import io.smallrye.openapi.runtime.io.discriminator.DiscriminatorWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsWriter;
import io.smallrye.openapi.runtime.io.xml.XmlWriter;

/**
 * Writing the Schema to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#schemaObject">schemaObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SchemaWriter {

    private SchemaWriter() {
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * 
     * @param parent
     * @param schemas
     */
    public static void writeSchemas(ObjectNode parent, Map<String, Schema> schemas) {
        writeSchemas(parent, schemas, ComponentsConstant.PROP_SCHEMAS);
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * 
     * @param parent
     * @param schemas
     */
    private static void writeSchemas(ObjectNode parent, Map<String, Schema> schemas, String propertyName) {
        if (schemas == null) {
            return;
        }
        ObjectNode schemasNode = parent.putObject(propertyName);
        for (String schemaName : schemas.keySet()) {
            writeSchema(schemasNode, schemas.get(schemaName), schemaName);
        }
    }

    /**
     * Writes a {@link Schema} to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    public static void writeSchema(ObjectNode parent, Schema model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        writeSchema(node, model);
    }

    /**
     * Writes the {@link Schema} model to the given node.
     * 
     * @param node
     * @param model
     */
    private static void writeSchema(ObjectNode node, Schema model) {
        JsonUtil.stringProperty(node, SchemaConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, SchemaConstant.PROP_FORMAT, model.getFormat());
        JsonUtil.stringProperty(node, SchemaConstant.PROP_TITLE, model.getTitle());
        JsonUtil.stringProperty(node, SchemaConstant.PROP_DESCRIPTION, model.getDescription());
        ObjectWriter.writeObject(node, SchemaConstant.PROP_DEFAULT, model.getDefaultValue());
        JsonUtil.bigDecimalProperty(node, SchemaConstant.PROP_MULTIPLE_OF, model.getMultipleOf());
        JsonUtil.bigDecimalProperty(node, SchemaConstant.PROP_MAXIMUM, model.getMaximum());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_EXCLUSIVE_MAXIMUM, model.getExclusiveMaximum());
        JsonUtil.bigDecimalProperty(node, SchemaConstant.PROP_MINIMUM, model.getMinimum());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_EXCLUSIVE_MINIMUM, model.getExclusiveMinimum());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MAX_LENGTH, model.getMaxLength());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MIN_LENGTH, model.getMinLength());
        JsonUtil.stringProperty(node, SchemaConstant.PROP_PATTERN, model.getPattern());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MAX_ITEMS, model.getMaxItems());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MIN_ITEMS, model.getMinItems());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_UNIQUE_ITEMS, model.getUniqueItems());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MAX_PROPERTIES, model.getMaxProperties());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MIN_PROPERTIES, model.getMinProperties());
        ObjectWriter.writeStringArray(node, model.getRequired(), SchemaConstant.PROP_REQUIRED);
        ObjectWriter.writeObjectArray(node, model.getEnumeration(), SchemaConstant.PROP_ENUM);
        JsonUtil.enumProperty(node, SchemaConstant.PROP_TYPE, model.getType());
        writeSchema(node, model.getItems(), SchemaConstant.PROP_ITEMS);
        writeSchemaList(node, model.getAllOf(), SchemaConstant.PROP_ALL_OF);
        writeSchemas(node, model.getProperties(), SchemaConstant.PROP_PROPERTIES);
        if (model.getAdditionalPropertiesBoolean() != null) {
            JsonUtil.booleanProperty(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES,
                    model.getAdditionalPropertiesBoolean());
        } else {
            writeSchema(node, (Schema) model.getAdditionalPropertiesSchema(),
                    SchemaConstant.PROP_ADDITIONAL_PROPERTIES);
        }
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_READ_ONLY, model.getReadOnly());
        XmlWriter.writeXML(node, model.getXml());
        ExternalDocsWriter.writeExternalDocumentation(node, model.getExternalDocs());
        ObjectWriter.writeObject(node, SchemaConstant.PROP_EXAMPLE, model.getExample());
        writeSchemaList(node, model.getOneOf(), SchemaConstant.PROP_ONE_OF);
        writeSchemaList(node, model.getAnyOf(), SchemaConstant.PROP_ANY_OF);
        writeSchema(node, model.getNot(), SchemaConstant.PROP_NOT);
        DiscriminatorWriter.writeDiscriminator(node, model.getDiscriminator());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_NULLABLE, model.getNullable());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_WRITE_ONLY, model.getWriteOnly());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_DEPRECATED, model.getDeprecated());
        ExtensionWriter.writeExtensions(node, model);
    }

    /**
     * Writes a list of {@link Schema} to the JSON tree.
     * 
     * @param parent
     * @param models
     * @param propertyName
     */
    private static void writeSchemaList(ObjectNode parent, List<Schema> models, String propertyName) {
        if (models == null) {
            return;
        }
        ArrayNode schemasNode = parent.putArray(propertyName);
        for (Schema schema : models) {
            writeSchema(schemasNode.addObject(), schema);
        }
    }
}
