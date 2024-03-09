package io.smallrye.openapi.runtime.io.media;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.media.XMLImpl;
import io.smallrye.openapi.runtime.io.ExternalDocumentationIO;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;

public class SchemaIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Schema, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_NAME = "name";
    private static final String PROP_PREFIX = "prefix";
    private static final String PROP_NAMESPACE = "namespace";
    private static final String PROP_WRAPPED = "wrapped";
    private static final String PROP_ATTRIBUTE = "attribute";

    private final DiscriminatorIO<V, A, O, AB, OB> discriminatorIO;
    private final ExternalDocumentationIO<V, A, O, AB, OB> externalDocIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public SchemaIO(IOContext<V, A, O, AB, OB> context, ExtensionIO<V, A, O, AB, OB> extensionIO) {
        super(context, Names.SCHEMA, Names.create(Schema.class));
        discriminatorIO = new DiscriminatorIO<>(context);
        externalDocIO = new ExternalDocumentationIO<>(context, extensionIO);
        this.extensionIO = extensionIO;
    }

    public DiscriminatorIO<V, A, O, AB, OB> discriminator() {
        return discriminatorIO;
    }

    @Override
    public Schema read(AnnotationInstance annotation) {
        return read(null, annotation);
    }

    @Override
    protected Schema read(String name, AnnotationInstance annotation) {
        return SchemaFactory.readSchema(scannerContext(), new SchemaImpl(name), annotation, Collections.emptyMap());
    }

    @Override
    public Schema readObject(O node) {
        IoLogging.logger.singleJsonObject("Schema");
        Schema schema = new SchemaImpl(jsonIO().getString(node, SchemaConstant.PROP_NAME));
        schema.setRef(readReference(node));
        schema.setFormat(jsonIO().getString(node, SchemaConstant.PROP_FORMAT));
        schema.setTitle(jsonIO().getString(node, SchemaConstant.PROP_TITLE));
        schema.setDescription(jsonIO().getString(node, SchemaConstant.PROP_DESCRIPTION));
        schema.setDefaultValue(jsonIO().fromJson(jsonIO().getValue(node, SchemaConstant.PROP_DEFAULT)));
        schema.setMultipleOf(jsonIO().getBigDecimal(node, SchemaConstant.PROP_MULTIPLE_OF));
        schema.setMaximum(jsonIO().getBigDecimal(node, SchemaConstant.PROP_MAXIMUM));
        schema.setExclusiveMaximum(jsonIO().getBoolean(node, SchemaConstant.PROP_EXCLUSIVE_MAXIMUM));
        schema.setMinimum(jsonIO().getBigDecimal(node, SchemaConstant.PROP_MINIMUM));
        schema.setExclusiveMinimum(jsonIO().getBoolean(node, SchemaConstant.PROP_EXCLUSIVE_MINIMUM));
        schema.setMaxLength(jsonIO().getInt(node, SchemaConstant.PROP_MAX_LENGTH));
        schema.setMinLength(jsonIO().getInt(node, SchemaConstant.PROP_MIN_LENGTH));
        schema.setPattern(jsonIO().getString(node, SchemaConstant.PROP_PATTERN));
        schema.setMaxItems(jsonIO().getInt(node, SchemaConstant.PROP_MAX_ITEMS));
        schema.setMinItems(jsonIO().getInt(node, SchemaConstant.PROP_MIN_ITEMS));
        schema.setUniqueItems(jsonIO().getBoolean(node, SchemaConstant.PROP_UNIQUE_ITEMS));
        schema.setMaxProperties(jsonIO().getInt(node, SchemaConstant.PROP_MAX_PROPERTIES));
        schema.setMinProperties(jsonIO().getInt(node, SchemaConstant.PROP_MIN_PROPERTIES));
        schema.setRequired(jsonIO().getArray(node, SchemaConstant.PROP_REQUIRED, jsonIO()::asString).orElse(null));
        schema.setEnumeration(jsonIO().getArray(node, SchemaConstant.PROP_ENUM, jsonIO()::fromJson).orElse(null));
        schema.setType(enumValue(jsonIO().getValue(node, SchemaConstant.PROP_TYPE), Schema.SchemaType.class));
        schema.setItems(jsonIO().getObject(node, SchemaConstant.PROP_ITEMS).map(this::readObject).orElse(null));
        schema.setNot(jsonIO().getObject(node, SchemaConstant.PROP_NOT).map(this::readObject).orElse(null));
        schema.setAllOf(jsonIO().getArray(node, SchemaConstant.PROP_ALL_OF, this::readValue).orElse(null));
        schema.setProperties(readMap(jsonIO().getValue(node, SchemaConstant.PROP_PROPERTIES)));

        V addlProperties = jsonIO().getValue(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES);

        if (jsonIO().isObject(addlProperties)) {
            schema.setAdditionalPropertiesSchema(readObject(jsonIO().asObject(addlProperties)));
        } else {
            schema.setAdditionalPropertiesBoolean(
                    jsonIO().getBoolean(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES));
        }

        schema.setReadOnly(jsonIO().getBoolean(node, SchemaConstant.PROP_READ_ONLY));
        schema.setXml(readXML(jsonIO().getValue(node, SchemaConstant.PROP_XML)));
        schema.setExternalDocs(externalDocIO.readValue(jsonIO().getValue(node, SchemaConstant.PROP_EXTERNAL_DOCS)));
        schema.setExample(jsonIO().fromJson(jsonIO().getValue(node, SchemaConstant.PROP_EXAMPLE)));
        schema.setOneOf(jsonIO().getArray(node, SchemaConstant.PROP_ONE_OF, this::readValue).orElse(null));
        schema.setAnyOf(jsonIO().getArray(node, SchemaConstant.PROP_ANY_OF, this::readValue).orElse(null));
        schema.setDiscriminator(discriminatorIO.readValue(jsonIO().getValue(node, SchemaConstant.PROP_DISCRIMINATOR)));
        schema.setNullable(jsonIO().getBoolean(node, SchemaConstant.PROP_NULLABLE));
        schema.setWriteOnly(jsonIO().getBoolean(node, SchemaConstant.PROP_WRITE_ONLY));
        schema.setDeprecated(jsonIO().getBoolean(node, SchemaConstant.PROP_DEPRECATED));
        schema.setExtensions(extensionIO.readMap(node));
        return schema;
    }

    public XML readXML(V node) {
        return Optional.ofNullable(node)
                .filter(jsonIO()::isObject)
                .map(jsonIO()::asObject)
                .map(object -> {
                    XML xml = new XMLImpl();
                    xml.setName(jsonIO().getString(node, PROP_NAME));
                    xml.setNamespace(jsonIO().getString(node, PROP_NAMESPACE));
                    xml.setPrefix(jsonIO().getString(node, PROP_PREFIX));
                    xml.setAttribute(jsonIO().getBoolean(node, PROP_ATTRIBUTE));
                    xml.setWrapped(jsonIO().getBoolean(node, PROP_WRAPPED));
                    xml.setExtensions(extensionIO.readMap(node));
                    return xml;
                })
                .orElse(null);
    }

    public Optional<O> write(Schema model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                setReference(node, model);
            } else {
                setIfPresent(node, SchemaConstant.PROP_FORMAT, jsonIO().toJson(model.getFormat()));
                setIfPresent(node, SchemaConstant.PROP_TITLE, jsonIO().toJson(model.getTitle()));
                setIfPresent(node, SchemaConstant.PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
                setIfPresent(node, SchemaConstant.PROP_DEFAULT, jsonIO().toJson(model.getDefaultValue()));
                setIfPresent(node, SchemaConstant.PROP_MULTIPLE_OF, jsonIO().toJson(model.getMultipleOf()));
                setIfPresent(node, SchemaConstant.PROP_MAXIMUM, jsonIO().toJson(model.getMaximum()));
                setIfPresent(node, SchemaConstant.PROP_EXCLUSIVE_MAXIMUM, jsonIO().toJson(model.getExclusiveMaximum()));
                setIfPresent(node, SchemaConstant.PROP_MINIMUM, jsonIO().toJson(model.getMinimum()));
                setIfPresent(node, SchemaConstant.PROP_EXCLUSIVE_MINIMUM, jsonIO().toJson(model.getExclusiveMinimum()));
                setIfPresent(node, SchemaConstant.PROP_MAX_LENGTH, jsonIO().toJson(model.getMaxLength()));
                setIfPresent(node, SchemaConstant.PROP_MIN_LENGTH, jsonIO().toJson(model.getMinLength()));
                setIfPresent(node, SchemaConstant.PROP_PATTERN, jsonIO().toJson(model.getPattern()));
                setIfPresent(node, SchemaConstant.PROP_MAX_ITEMS, jsonIO().toJson(model.getMaxItems()));
                setIfPresent(node, SchemaConstant.PROP_MIN_ITEMS, jsonIO().toJson(model.getMinItems()));
                setIfPresent(node, SchemaConstant.PROP_UNIQUE_ITEMS, jsonIO().toJson(model.getUniqueItems()));
                setIfPresent(node, SchemaConstant.PROP_MAX_PROPERTIES, jsonIO().toJson(model.getMaxProperties()));
                setIfPresent(node, SchemaConstant.PROP_MIN_PROPERTIES, jsonIO().toJson(model.getMinProperties()));
                setIfPresent(node, SchemaConstant.PROP_REQUIRED, jsonIO().toJson(model.getRequired()));
                setIfPresent(node, SchemaConstant.PROP_ENUM, jsonIO().toJson(model.getEnumeration()));
                setIfPresent(node, SchemaConstant.PROP_TYPE, jsonIO().toJson(model.getType()));
                setIfPresent(node, SchemaConstant.PROP_ITEMS, write(model.getItems()));
                setIfPresent(node, SchemaConstant.PROP_ALL_OF, write(model.getAllOf()));
                setIfPresent(node, SchemaConstant.PROP_PROPERTIES, write(model.getProperties()));
                if (model.getAdditionalPropertiesBoolean() != null) {
                    setIfPresent(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES,
                            jsonIO().toJson(model.getAdditionalPropertiesBoolean()));
                } else {
                    setIfPresent(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES, write(model.getAdditionalPropertiesSchema()));
                }
                setIfPresent(node, SchemaConstant.PROP_READ_ONLY, jsonIO().toJson(model.getReadOnly()));
                setIfPresent(node, SchemaConstant.PROP_XML, write(model.getXml()));
                setIfPresent(node, SchemaConstant.PROP_EXTERNAL_DOCS, externalDocIO.write(model.getExternalDocs()));
                setIfPresent(node, SchemaConstant.PROP_EXAMPLE, jsonIO().toJson(model.getExample()));
                setIfPresent(node, SchemaConstant.PROP_ONE_OF, write(model.getOneOf()));
                setIfPresent(node, SchemaConstant.PROP_ANY_OF, write(model.getAnyOf()));
                setIfPresent(node, SchemaConstant.PROP_NOT, write(model.getNot()));
                setIfPresent(node, SchemaConstant.PROP_DISCRIMINATOR, discriminatorIO.write(model.getDiscriminator()));
                setIfPresent(node, SchemaConstant.PROP_NULLABLE, jsonIO().toJson(model.getNullable()));
                setIfPresent(node, SchemaConstant.PROP_WRITE_ONLY, jsonIO().toJson(model.getWriteOnly()));
                setIfPresent(node, SchemaConstant.PROP_DEPRECATED, jsonIO().toJson(model.getDeprecated()));
                setAllIfPresent(node, extensionIO.write(model));
            }

            return node;
        }).map(jsonIO()::buildObject);
    }

    /**
     * Writes a list of {@link Schema} to the JSON tree.
     *
     * @param parent
     * @param models
     * @param propertyName
     */
    private Optional<A> write(List<Schema> models) {
        return optionalJsonArray(models).map(array -> {
            models.forEach(model -> write(model).ifPresent(v -> jsonIO().add(array, v)));
            return array;
        }).map(jsonIO()::buildArray);
    }

    public Optional<O> write(XML model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_NAME, jsonIO().toJson(model.getName()));
            setIfPresent(node, PROP_NAMESPACE, jsonIO().toJson(model.getNamespace()));
            setIfPresent(node, PROP_PREFIX, jsonIO().toJson(model.getPrefix()));
            setIfPresent(node, PROP_ATTRIBUTE, jsonIO().toJson(model.getAttribute()));
            setIfPresent(node, PROP_WRAPPED, jsonIO().toJson(model.getWrapped()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
