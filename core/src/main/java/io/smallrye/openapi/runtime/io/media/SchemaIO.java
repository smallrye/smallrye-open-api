package io.smallrye.openapi.runtime.io.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.media.XMLImpl;
import io.smallrye.openapi.runtime.io.ExternalDocumentationIO;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class SchemaIO extends MapModelIO<Schema> implements ReferenceIO {

    private static final String PROP_NAME = "name";
    private static final String PROP_PREFIX = "prefix";
    private static final String PROP_NAMESPACE = "namespace";
    private static final String PROP_WRAPPED = "wrapped";
    private static final String PROP_ATTRIBUTE = "attribute";

    private final DiscriminatorIO discriminatorIO;
    private final ExternalDocumentationIO externalDocIO;
    private final ExtensionIO extensionIO;

    public SchemaIO(AnnotationScannerContext context) {
        super(context, Names.SCHEMA, Names.create(Schema.class));
        discriminatorIO = new DiscriminatorIO(context);
        externalDocIO = new ExternalDocumentationIO(context);
        extensionIO = new ExtensionIO(context);
    }

    public DiscriminatorIO discriminator() {
        return discriminatorIO;
    }

    @Override
    public Schema read(AnnotationInstance annotation) {
        return read(null, annotation);
    }

    @Override
    protected Schema read(String name, AnnotationInstance annotation) {
        return SchemaFactory.readSchema(context, new SchemaImpl(name), annotation, Collections.emptyMap());
    }

    @Override
    public Schema read(ObjectNode node) {
        IoLogging.logger.singleJsonObject("Schema");
        String name = JsonUtil.stringProperty(node, SchemaConstant.PROP_NAME);

        Schema schema = new SchemaImpl(name);
        schema.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        schema.setFormat(JsonUtil.stringProperty(node, SchemaConstant.PROP_FORMAT));
        schema.setTitle(JsonUtil.stringProperty(node, SchemaConstant.PROP_TITLE));
        schema.setDescription(JsonUtil.stringProperty(node, SchemaConstant.PROP_DESCRIPTION));
        schema.setDefaultValue(JsonUtil.readObject(node.get(SchemaConstant.PROP_DEFAULT)));
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
        schema.setItems(read(node.get(SchemaConstant.PROP_ITEMS)));
        schema.setNot(read(node.get(SchemaConstant.PROP_NOT)));
        schema.setAllOf(readList(node.get(SchemaConstant.PROP_ALL_OF)));
        schema.setProperties(readMap(node.get(SchemaConstant.PROP_PROPERTIES)));

        if (node.has(SchemaConstant.PROP_ADDITIONAL_PROPERTIES)
                && node.get(SchemaConstant.PROP_ADDITIONAL_PROPERTIES).isObject()) {
            schema.setAdditionalPropertiesSchema(read(node.get(SchemaConstant.PROP_ADDITIONAL_PROPERTIES)));
        } else {
            schema.setAdditionalPropertiesBoolean(
                    JsonUtil.booleanProperty(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES).orElse(null));
        }

        schema.setReadOnly(JsonUtil.booleanProperty(node, SchemaConstant.PROP_READ_ONLY).orElse(null));
        schema.setXml(readXML(node.get(SchemaConstant.PROP_XML)));
        schema.setExternalDocs(externalDocIO.read(node.get(SchemaConstant.PROP_EXTERNAL_DOCS)));
        schema.setExample(JsonUtil.readObject(node.get(SchemaConstant.PROP_EXAMPLE)));
        schema.setOneOf(readList(node.get(SchemaConstant.PROP_ONE_OF)));
        schema.setAnyOf(readList(node.get(SchemaConstant.PROP_ANY_OF)));
        schema.setNot(read(node.get(SchemaConstant.PROP_NOT)));
        schema.setDiscriminator(discriminatorIO.read(node.get(SchemaConstant.PROP_DISCRIMINATOR)));
        schema.setNullable(JsonUtil.booleanProperty(node, SchemaConstant.PROP_NULLABLE).orElse(null));
        schema.setWriteOnly(JsonUtil.booleanProperty(node, SchemaConstant.PROP_WRITE_ONLY).orElse(null));
        schema.setDeprecated(JsonUtil.booleanProperty(node, SchemaConstant.PROP_DEPRECATED).orElse(null));
        schema.setExtensions(extensionIO.readMap(node));
        return schema;
    }

    public XML readXML(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isObject)
                .map(ObjectNode.class::cast)
                .map(object -> {
                    XML xml = new XMLImpl();
                    xml.setName(JsonUtil.stringProperty(node, PROP_NAME));
                    xml.setNamespace(JsonUtil.stringProperty(node, PROP_NAMESPACE));
                    xml.setPrefix(JsonUtil.stringProperty(node, PROP_PREFIX));
                    xml.setAttribute(JsonUtil.booleanProperty(node, PROP_ATTRIBUTE).orElse(null));
                    xml.setWrapped(JsonUtil.booleanProperty(node, PROP_WRAPPED).orElse(null));
                    xml.setExtensions(extensionIO.readMap(node));
                    return xml;
                })
                .orElse(null);
    }

    private static Schema.SchemaType readSchemaType(final JsonNode node) {
        if (node != null && node.isTextual()) {
            String strval = node.asText();
            return Schema.SchemaType.valueOf(strval.toUpperCase(Locale.ROOT));
        }
        return null;
    }

    /**
     * Reads a list of schemas.
     *
     * @param node the json array
     * @return List of Schema models
     */
    private List<Schema> readList(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isArray)
                .map(ArrayNode.class::cast)
                .map(ArrayNode::elements)
                .map(elements -> Spliterators.spliteratorUnknownSize(elements, Spliterator.ORDERED))
                .map(elements -> StreamSupport.stream(elements, false))
                .map(elements -> elements.filter(JsonNode::isObject)
                        .map(ObjectNode.class::cast)
                        .map(this::read)
                        .collect(Collectors.toCollection(ArrayList::new)))
                .orElse(null);
    }

    public Optional<ObjectNode> write(Schema model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
            } else {
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
                setIfPresent(node, SchemaConstant.PROP_ITEMS, write(model.getItems()));
                setIfPresent(node, SchemaConstant.PROP_ALL_OF, write(model.getAllOf()));
                setIfPresent(node, SchemaConstant.PROP_PROPERTIES, write(model.getProperties()));
                if (model.getAdditionalPropertiesBoolean() != null) {
                    JsonUtil.booleanProperty(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES,
                            model.getAdditionalPropertiesBoolean());
                } else {
                    setIfPresent(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES, write(model.getAdditionalPropertiesSchema()));
                }
                JsonUtil.booleanProperty(node, SchemaConstant.PROP_READ_ONLY, model.getReadOnly());
                setIfPresent(node, SchemaConstant.PROP_XML, write(model.getXml()));
                setIfPresent(node, SchemaConstant.PROP_EXTERNAL_DOCS, externalDocIO.write(model.getExternalDocs()));
                ObjectWriter.writeObject(node, SchemaConstant.PROP_EXAMPLE, model.getExample());
                setIfPresent(node, SchemaConstant.PROP_ONE_OF, write(model.getOneOf()));
                setIfPresent(node, SchemaConstant.PROP_ANY_OF, write(model.getAnyOf()));
                setIfPresent(node, SchemaConstant.PROP_NOT, write(model.getNot()));
                setIfPresent(node, SchemaConstant.PROP_DISCRIMINATOR, discriminatorIO.write(model.getDiscriminator()));
                JsonUtil.booleanProperty(node, SchemaConstant.PROP_NULLABLE, model.getNullable());
                JsonUtil.booleanProperty(node, SchemaConstant.PROP_WRITE_ONLY, model.getWriteOnly());
                JsonUtil.booleanProperty(node, SchemaConstant.PROP_DEPRECATED, model.getDeprecated());
                setAllIfPresent(node, extensionIO.write(model));
            }

            return node;
        });
    }

    /**
     * Writes a list of {@link Schema} to the JSON tree.
     *
     * @param parent
     * @param models
     * @param propertyName
     */
    private Optional<ArrayNode> write(List<Schema> models) {
        return optionalJsonArray(models)
                .map(node -> {
                    models.stream().map(this::write).map(Optional::get).forEach(node::add);
                    return node;
                });
    }

    public Optional<ObjectNode> write(XML model) {
        return optionalJsonObject(model).map(node -> {
            JsonUtil.stringProperty(node, PROP_NAME, model.getName());
            JsonUtil.stringProperty(node, PROP_NAMESPACE, model.getNamespace());
            JsonUtil.stringProperty(node, PROP_PREFIX, model.getPrefix());
            JsonUtil.booleanProperty(node, PROP_ATTRIBUTE, model.getAttribute());
            JsonUtil.booleanProperty(node, PROP_WRAPPED, model.getWrapped());
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }
}
