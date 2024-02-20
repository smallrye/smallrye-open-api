package io.smallrye.openapi.runtime.io.media;

import static io.smallrye.openapi.runtime.io.schema.DataType.listOf;
import static io.smallrye.openapi.runtime.io.schema.DataType.type;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROPERTIES_DATA_TYPES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
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
import io.smallrye.openapi.runtime.io.schema.DataType;
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
    public Schema readValue(V node) {
        if (node == null) {
            return null;
        }

        if (jsonIO().isBoolean(node)) {
            return new SchemaImpl().booleanSchema(jsonIO().asBoolean(node));
        }

        if (jsonIO().isObject(node)) {
            return readObject(jsonIO().asObject(node));
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Schema readObject(O node) {
        IoLogging.logger.singleJsonObject("Schema");
        String name = getName(node);
        SchemaImpl schema = new SchemaImpl(name);
        String dialect = jsonIO().getString(node, SchemaConstant.PROP_SCHEMA_DIALECT);
        if (dialect == null || dialect.equals(SchemaConstant.DIALECT_OAS31)
                || dialect.equals(SchemaConstant.DIALECT_JSON_2020_12)) {
            populateSchemaObject(schema, node);
        } else {
            schema.getDataMap().putAll((Map<? extends String, ? extends Object>) jsonIO().fromJson(node));
        }
        return schema;
    }

    private void populateSchemaObject(SchemaImpl schema, O node) {
        Map<String, Object> dataMap = schema.getDataMap();

        // Special handling for type since it can be an array or a string and we want to convert
        V typeNode = jsonIO().getValue(node, PROP_TYPE);
        if (typeNode != null) {
            if (jsonIO().isString(typeNode)) {
                ArrayList<Object> typeList = new ArrayList<>();
                typeList.add(readJson(typeNode, type(Schema.SchemaType.class)));
                dataMap.put(PROP_TYPE, typeList);
            } else {
                dataMap.put(PROP_TYPE, readJson(typeNode, listOf(type(Schema.SchemaType.class))));
            }
        }

        // Read known fields
        for (Map.Entry<String, DataType> entry : SchemaConstant.PROPERTIES_DATA_TYPES.entrySet()) {
            String key = entry.getKey();
            DataType type = entry.getValue();
            V fieldNode = jsonIO().getValue(node, key);
            if (fieldNode != null) {
                dataMap.put(key, readJson(fieldNode, type));
            }
        }

        // Read unknown fields
        for (Entry<String, V> entry : jsonIO().properties(node)) {
            String name = entry.getKey();
            V fieldNode = entry.getValue();
            if (!PROPERTIES_DATA_TYPES.containsKey(name) && !name.equals(PROP_TYPE) && !name.equals(PROP_NAME)) {
                dataMap.put(name, jsonIO().fromJson(fieldNode));
            }
        }
    }

    private String getName(O node) {
        V name = jsonIO().getValue(node, PROP_NAME);
        if (jsonIO().isString(name)) {
            return jsonIO().asString(name);
        } else {
            return null;
        }
    }

    private Object readJson(V node, DataType desiredType) {
        if (jsonIO().isObject(node) && desiredType.type == DataType.Type.MAP) {
            Map<String, Object> result = new HashMap<>();
            O object = jsonIO().asObject(node);
            for (Entry<String, V> entry : jsonIO().properties(object)) {
                result.put(entry.getKey(), readJson(entry.getValue(), desiredType.content));
            }
            return result;
        } else if (jsonIO().isArray(node) && desiredType.type == DataType.Type.LIST) {
            List<Object> result = new ArrayList<>();
            A array = jsonIO().asArray(node);
            for (V element : jsonIO().entries(array)) {
                result.add(readJson(element, desiredType.content));
            }
            return result;
        } else if (desiredType.type == DataType.Type.OBJECT) {
            return readValue(node, desiredType.clazz);
        } else {
            return jsonIO().fromJson(node);
        }
    }

    /**
     * Convert JSON value node to an object when we have a desired type
     * <p>
     * The JSON value will be converted to the desired type if possible or returned as its native type if not.
     *
     * @param node the JSON node
     * @param desiredType the type that we want to be returned
     * @return an object which represents the JSON node, which may or may not be of the desired type
     */
    @SuppressWarnings("unchecked")
    private Object readValue(V node, Class<?> desiredType) {
        // Handles string, number and boolean types
        Object result = jsonIO().fromJson(node, desiredType);
        if (result != null) {
            return result;
        }

        if (Enum.class.isAssignableFrom(desiredType)) {
            result = enumValue(node, desiredType.asSubclass(Enum.class));
            if (result != null) {
                return result;
            }
        }

        if (jsonIO().isObject(node)) {
            if (desiredType == Schema.class) {
                return readValue(node);
            }
            if (desiredType == XML.class) {
                return readXML(node);
            }
            if (desiredType == ExternalDocumentation.class) {
                return externalDocIO.readValue(node);
            }
            if (desiredType == Discriminator.class) {
                return discriminatorIO.readValue(node);
            }
        }
        return jsonIO().fromJson(node);
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

    public Optional<? extends V> write(Schema model) {
        if (model == null) {
            return Optional.empty();
        }

        if (model.getBooleanSchema() != null) {
            return jsonIO().toJson(model.getBooleanSchema());
        }

        SchemaImpl impl = (SchemaImpl) model;
        Map<String, Object> data = impl.getDataMap();
        return writeMap(data);
    }

    private Optional<? extends V> writeObject(Object value) {
        if (value instanceof Schema) {
            return write((Schema) value);
        } else if (value instanceof XML) {
            return write((XML) value);
        } else if (value instanceof ExternalDocumentation) {
            return externalDocIO.write((ExternalDocumentation) value);
        } else if (value instanceof Discriminator) {
            return discriminatorIO.write((Discriminator) value);
        } else if (value instanceof List<?>) {
            return writeList((List<?>) value);
        } else if (value instanceof Map<?, ?>) {
            return writeMap((Map<?, ?>) value);
        } else {
            return jsonIO().toJson(value);
        }
    }

    private Optional<O> writeMap(Map<?, ?> map) {
        return optionalJsonObject(map).map(result -> {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String))
                    continue;
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                if (PROP_TYPE.equals(key)) {
                    // Flatten one-entry type lists
                    if (value instanceof List && ((List<?>) value).size() == 1) {
                        value = ((List<?>) value).get(0);
                    }
                }
                setIfPresent(result, key, writeObject(value));
            }
            return result;
        }).map(jsonIO()::buildObject);
    }

    private Optional<A> writeList(List<?> list) {
        return optionalJsonArray(list).map(result -> {
            for (Object entry : list) {
                writeObject(entry).ifPresent(v -> jsonIO().add(result, v));
            }
            return jsonIO().buildArray(result);
        });
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
