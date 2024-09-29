package io.smallrye.openapi.runtime.io.media;

import static io.smallrye.openapi.runtime.io.schema.DataType.listOf;
import static io.smallrye.openapi.runtime.io.schema.DataType.type;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROPERTIES_DATA_TYPES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_EXCLUSIVE_MAXIMUM;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_EXCLUSIVE_MINIMUM;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MAXIMUM;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MINIMUM;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_NULLABLE;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_REF;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_TYPE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.eclipse.microprofile.openapi.models.media.XML;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.media.XMLImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IOContext.OpenApiVersion;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.schema.DataType;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.util.ModelUtil;

public class SchemaIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Schema, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_NAME = "name";
    private static final String PROP_PREFIX = "prefix";
    private static final String PROP_NAMESPACE = "namespace";
    private static final String PROP_WRAPPED = "wrapped";
    private static final String PROP_ATTRIBUTE = "attribute";

    public SchemaIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.SCHEMA, Names.create(Schema.class));
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
        schema.setRef(jsonIO().getJsonString(node, PROP_REF));

        if (openApiVersion() == OpenApiVersion.V3_1) {
            String dialect = jsonIO().getString(node, SchemaConstant.PROP_SCHEMA_DIALECT);
            if (dialect == null || dialect.equals(SchemaConstant.DIALECT_OAS31)
                    || dialect.equals(SchemaConstant.DIALECT_JSON_2020_12)) {
                populateSchemaObject(schema, node);
            } else {
                schema.getDataMap().putAll((Map<? extends String, ? extends Object>) jsonIO().fromJson(node));
            }
        } else {
            populateSchemaObject30(schema, node);
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
            if (!PROPERTIES_DATA_TYPES.containsKey(name) && !name.equals(PROP_TYPE) && !name.equals(PROP_NAME)
                    && !name.equals(PROP_REF)) {
                dataMap.put(name, jsonIO().fromJson(fieldNode));
            }
        }
    }

    private void populateSchemaObject30(SchemaImpl schema, O node) {
        Map<String, Object> dataMap = schema.getDataMap();

        // Call our internal methods for type/nullable handling
        SchemaImpl.setType(schema, enumValue(jsonIO().getValue(node, PROP_TYPE), Schema.SchemaType.class));
        SchemaImpl.setNullable(schema, jsonIO().getBoolean(node, PROP_NULLABLE));

        // Translate minimum
        BigDecimal minimum = jsonIO().getBigDecimal(node, PROP_MINIMUM);
        if (minimum != null) {
            if (jsonIO().getBoolean(node, PROP_EXCLUSIVE_MINIMUM) == Boolean.TRUE) {
                schema.setExclusiveMinimum(minimum);
            } else {
                schema.setMinimum(minimum);
            }
        }

        // Translate maximum
        BigDecimal maximum = jsonIO().getBigDecimal(node, PROP_MAXIMUM);
        if (maximum != null) {
            if (jsonIO().getBoolean(node, PROP_EXCLUSIVE_MAXIMUM) == Boolean.TRUE) {
                schema.setExclusiveMaximum(maximum);
            } else {
                schema.setMaximum(maximum);
            }
        }

        // Read known fields
        for (Map.Entry<String, DataType> entry : SchemaConstant.PROPERTIES_DATA_TYPES_3_0.entrySet()) {
            String key = entry.getKey();
            DataType dataType = entry.getValue();
            V fieldNode = jsonIO().getValue(node, key);
            if (fieldNode != null) {
                dataMap.put(key, readJson(fieldNode, dataType));
            }
        }

        // Read extensions
        extensionIO().readMap(node).forEach(schema::addExtension);

        // Move allOf[{$ref=....}] to the top level
        List<Schema> allOf = schema.getAllOf();
        if (schema.getRef() == null && allOf != null) {
            List<Schema> allOfRefs = allOf.stream()
                    .filter(s -> isSoloRef(s))
                    .collect(Collectors.toList());

            if (allOfRefs.size() == 1) {
                Schema refSchema = allOfRefs.get(0);
                schema.removeAllOf(refSchema);
                schema.setRef(refSchema.getRef());
                if (schema.getAllOf().isEmpty()) {
                    schema.setAllOf(null);
                }
            }
        }

        // Detect {$ref=....,nullable=true} and convert to anyOf[{$ref=...}, {type=null}]
        if (schema.getRef() != null && schema.getType() == null && SchemaImpl.getNullable(schema) == Boolean.TRUE) {
            List<Schema> newAnyOfSchemas = new ArrayList<>();
            newAnyOfSchemas.add(new SchemaImpl().ref(schema.getRef()));
            newAnyOfSchemas.add(new SchemaImpl().addType(SchemaType.NULL));
            if (schema.getAnyOf() == null || schema.getAnyOf().isEmpty()) {
                schema.setAnyOf(newAnyOfSchemas);
            } else {
                schema.addAllOf(new SchemaImpl().anyOf(newAnyOfSchemas));
            }
            schema.setRef(null);
            SchemaImpl.setNullable(schema, null);
        }

        // Detect {enum=[null]} and convert to {type=null}
        // Detect {enum=[value]} and convert to {const=value}
        List<Object> enumeration = schema.getEnumeration();
        if (enumeration != null && enumeration.size() == 1) {
            if (enumeration.get(0) == null) {
                schema.setType(Collections.singletonList(SchemaType.NULL));
                schema.setEnumeration(null);
            } else if (schema.getConstValue() == null) {
                schema.setConstValue(enumeration.get(0));
                schema.setEnumeration(null);
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

        if (desiredType == Schema.class) {
            return readValue(node);
        }
        if (desiredType == XML.class) {
            return readXML(node);
        }
        if (desiredType == ExternalDocumentation.class) {
            return extDocIO().readValue(node);
        }
        if (desiredType == Discriminator.class) {
            return discriminatorIO().readValue(node);
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
                    xml.setExtensions(extensionIO().readMap(node));
                    return xml;
                })
                .orElse(null);
    }

    public Optional<? extends V> write(Schema model) {
        if (model == null) {
            return Optional.empty();
        }

        if (openApiVersion() == OpenApiVersion.V3_1) {
            return write31(model);
        } else {
            return write30(model);
        }
    }

    private Optional<? extends V> write31(Schema model) {
        if (model.getBooleanSchema() != null) {
            return jsonIO().toJson(model.getBooleanSchema());
        }

        SchemaImpl impl = (SchemaImpl) model;
        Map<String, Object> data = impl.getDataMap();
        return writeMap(data);
    }

    @SuppressWarnings("deprecation")
    public Optional<O> write30(Schema model) {
        return optionalJsonObject(model).map(node -> {
            ReplacementFields fields = compute30ReplacementFields(model);
            if (fields.ref != null && !fields.ref.isEmpty()) {
                setReference(node, model);
            } else {
                setIfPresent(node, SchemaConstant.PROP_FORMAT, jsonIO().toJson(model.getFormat()));
                setIfPresent(node, SchemaConstant.PROP_TITLE, jsonIO().toJson(model.getTitle()));
                setIfPresent(node, SchemaConstant.PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
                setIfPresent(node, SchemaConstant.PROP_DEFAULT, jsonIO().toJson(model.getDefaultValue()));
                setIfPresent(node, SchemaConstant.PROP_MULTIPLE_OF, jsonIO().toJson(model.getMultipleOf()));
                setIfPresent(node, SchemaConstant.PROP_MAXIMUM, jsonIO().toJson(fields.maximum));
                setIfPresent(node, SchemaConstant.PROP_EXCLUSIVE_MAXIMUM, jsonIO().toJson(fields.exclusiveMaximum));
                setIfPresent(node, SchemaConstant.PROP_MINIMUM, jsonIO().toJson(fields.minimum));
                setIfPresent(node, SchemaConstant.PROP_EXCLUSIVE_MINIMUM, jsonIO().toJson(fields.exclusiveMinimum));
                setIfPresent(node, SchemaConstant.PROP_MAX_LENGTH, jsonIO().toJson(model.getMaxLength()));
                setIfPresent(node, SchemaConstant.PROP_MIN_LENGTH, jsonIO().toJson(model.getMinLength()));
                setIfPresent(node, SchemaConstant.PROP_PATTERN, jsonIO().toJson(model.getPattern()));
                setIfPresent(node, SchemaConstant.PROP_MAX_ITEMS, jsonIO().toJson(model.getMaxItems()));
                setIfPresent(node, SchemaConstant.PROP_MIN_ITEMS, jsonIO().toJson(model.getMinItems()));
                setIfPresent(node, SchemaConstant.PROP_UNIQUE_ITEMS, jsonIO().toJson(model.getUniqueItems()));
                setIfPresent(node, SchemaConstant.PROP_MAX_PROPERTIES, jsonIO().toJson(model.getMaxProperties()));
                setIfPresent(node, SchemaConstant.PROP_MIN_PROPERTIES, jsonIO().toJson(model.getMinProperties()));
                setIfPresent(node, SchemaConstant.PROP_REQUIRED, jsonIO().toJson(model.getRequired()));
                setIfPresent(node, SchemaConstant.PROP_ENUM, jsonIO().toJson(fields.enumeration));
                setIfPresent(node, SchemaConstant.PROP_TYPE, jsonIO().toJson(fields.type));
                setIfPresent(node, SchemaConstant.PROP_ITEMS, write(model.getItems()));
                setIfPresent(node, SchemaConstant.PROP_ALL_OF, writeList(fields.allOf));
                setIfPresent(node, SchemaConstant.PROP_PROPERTIES, writeMap(model.getProperties()));
                if (model.getAdditionalPropertiesBoolean() != null) {
                    setIfPresent(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES,
                            jsonIO().toJson(model.getAdditionalPropertiesBoolean()));
                } else {
                    setIfPresent(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES, write(model.getAdditionalPropertiesSchema()));
                }
                setIfPresent(node, SchemaConstant.PROP_READ_ONLY, jsonIO().toJson(model.getReadOnly()));
                setIfPresent(node, SchemaConstant.PROP_XML, write(model.getXml()));
                setIfPresent(node, SchemaConstant.PROP_EXTERNAL_DOCS, extDocIO().write(model.getExternalDocs()));
                setIfPresent(node, SchemaConstant.PROP_EXAMPLE, jsonIO().toJson(fields.example));
                setIfPresent(node, SchemaConstant.PROP_ONE_OF, writeList(model.getOneOf()));
                setIfPresent(node, SchemaConstant.PROP_ANY_OF, writeList(fields.anyOf));
                setIfPresent(node, SchemaConstant.PROP_NOT, write(model.getNot()));
                setIfPresent(node, SchemaConstant.PROP_DISCRIMINATOR, discriminatorIO().write(model.getDiscriminator()));
                setIfPresent(node, SchemaConstant.PROP_NULLABLE, jsonIO().toJson(fields.nullable));
                setIfPresent(node, SchemaConstant.PROP_WRITE_ONLY, jsonIO().toJson(model.getWriteOnly()));
                setIfPresent(node, SchemaConstant.PROP_DEPRECATED, jsonIO().toJson(model.getDeprecated()));
                setAllIfPresent(node, extensionIO().write(model));
            }

            return node;
        }).map(jsonIO()::buildObject);
    }

    @SuppressWarnings("deprecation")
    private ReplacementFields compute30ReplacementFields(Schema schema31) {
        ReplacementFields result = new ReplacementFields();

        // Transform types and nullable
        List<SchemaType> types = schema31.getType();
        if (types != null) {
            result.type = types.stream().filter(t -> t != SchemaType.NULL).findFirst().orElse(null);
            result.nullable = SchemaImpl.getNullable(schema31);
        }

        // Convert type=null to enum=[null] and const=value to enum=[value]
        result.enumeration = schema31.getEnumeration();
        if (result.type == null && result.nullable == Boolean.TRUE) {
            result.nullable = null;
            result.enumeration = Collections.singletonList(null);
        } else if (schema31.getConstValue() != null) {
            result.enumeration = Collections.singletonList(schema31.getConstValue());
        }

        // Convert numeric exclusiveMinimum to boolean
        BigDecimal oldMinimum = schema31.getMinimum();
        BigDecimal oldExclusiveMinimum = schema31.getExclusiveMinimum();
        if (oldMinimum != null) {
            result.minimum = oldMinimum;
            if (oldExclusiveMinimum != null && oldExclusiveMinimum.compareTo(oldMinimum) >= 0) {
                result.minimum = oldExclusiveMinimum;
                result.exclusiveMinimum = Boolean.TRUE;
            }
        } else if (oldExclusiveMinimum != null) {
            result.minimum = oldExclusiveMinimum;
            result.exclusiveMinimum = Boolean.TRUE;
        }

        // Convert numeric exclusiveMaximum to boolean
        BigDecimal oldMaximum = schema31.getMaximum();
        BigDecimal oldExclusiveMaximum = schema31.getExclusiveMaximum();
        if (oldMaximum != null) {
            result.maximum = oldMaximum;
            if (oldExclusiveMaximum != null && oldExclusiveMaximum.compareTo(oldMaximum) <= 0) {
                result.maximum = oldExclusiveMaximum;
                result.exclusiveMaximum = Boolean.TRUE;
            }
        } else if (oldExclusiveMaximum != null) {
            result.maximum = oldExclusiveMaximum;
            result.exclusiveMaximum = Boolean.TRUE;
        }

        // Transform example
        result.example = schema31.getExample();
        if (result.example == null) {
            result.example = Optional.ofNullable(schema31.getExamples())
                    .flatMap(l -> l.stream().findFirst())
                    .orElse(null);
        }

        result.ref = schema31.getRef();
        result.allOf = schema31.getAllOf();
        result.anyOf = schema31.getAnyOf();

        // If $ref is used with any other properties, move it to an allOf
        if (result.ref != null && !isSoloRef(schema31)) {
            result.ref = null;
            Schema refSchema = new SchemaImpl().ref(schema31.getRef());
            result.allOf = ModelUtil.replace(result.allOf, ArrayList::new); // replace first because result.allOf may be immutable
            result.allOf = ModelUtil.add(refSchema, result.allOf, ArrayList::new);
        }

        // If we have anyOf = [{type=null}, {$ref=...}], remove it and set nullable and allOf = [{$ref=...}]
        if (result.anyOf != null && result.anyOf.size() == 2 && result.ref == null && result.type == null) {
            Optional<Schema> typeNullSchema = result.anyOf.stream().filter(s -> isSoloTypeNull(s)).findFirst();
            Optional<Schema> refSchema = result.anyOf.stream().filter(s -> isSoloRef(s)).findFirst();
            if (typeNullSchema.isPresent() && refSchema.isPresent()) {
                result.anyOf = null;
                result.nullable = Boolean.TRUE;
                result.allOf = ModelUtil.replace(result.allOf, ArrayList::new); // replace first because result.allOf may be immutable
                result.allOf = ModelUtil.add(refSchema.get(), result.allOf, ArrayList::new);
            }
        }

        return result;
    }

    private Optional<? extends V> writeObject(Object value) {
        if (value instanceof Schema) {
            return write((Schema) value);
        } else if (value instanceof XML) {
            return write((XML) value);
        } else if (value instanceof Constructible) {
            return writeConstructible((Constructible) value);
        } else if (value instanceof List<?>) {
            return writeList((List<?>) value);
        } else if (value instanceof Map<?, ?>) {
            return writeMap((Map<?, ?>) value);
        } else {
            return jsonIO().toJson(value);
        }
    }

    private Optional<? extends V> writeConstructible(Constructible value) {
        // Java 21 cannot come soon enough
        if (value instanceof ExternalDocumentation) {
            return extDocIO().write((ExternalDocumentation) value);
        } else if (value instanceof Discriminator) {
            return discriminatorIO().write((Discriminator) value);
        } else if (value instanceof Components) {
            return componentsIO().write((Components) value);
        } else if (value instanceof OpenAPI) {
            return openApiDefinitionIO().write((OpenAPI) value);
        } else if (value instanceof Operation) {
            return operationIO().write((Operation) value);
        } else if (value instanceof PathItem) {
            return pathItemIO().write((PathItem) value);
        } else if (value instanceof Paths) {
            return pathsIO().write((Paths) value);
        } else if (value instanceof Callback) {
            return callbackIO().write((Callback) value);
        } else if (value instanceof Header) {
            return headerIO().write((Header) value);
        } else if (value instanceof Contact) {
            return contactIO().write((Contact) value);
        } else if (value instanceof Info) {
            return infoIO().write((Info) value);
        } else if (value instanceof License) {
            return licenseIO().write((License) value);
        } else if (value instanceof Link) {
            return linkIO().write((Link) value);
        } else if (value instanceof Content) {
            return contentIO().write((Content) value);
        } else if (value instanceof Encoding) {
            return encodingIO().write((Encoding) value);
        } else if (value instanceof Example) {
            return exampleObjectIO().write((Example) value);
        } else if (value instanceof MediaType) {
            return mediaTypeIO().write((MediaType) value);
        } else if (value instanceof Parameter) {
            return parameterIO().write((Parameter) value);
        } else if (value instanceof RequestBody) {
            return requestBodyIO().write((RequestBody) value);
        } else if (value instanceof APIResponse) {
            return apiResponseIO().write((APIResponse) value);
        } else if (value instanceof APIResponses) {
            return apiResponsesIO().write((APIResponses) value);
        } else if (value instanceof OAuthFlow) {
            return oauthFlowIO().write((OAuthFlow) value);
        } else if (value instanceof OAuthFlows) {
            return oauthFlowsIO().write((OAuthFlows) value);
        } else if (value instanceof SecurityRequirement) {
            return securityRequirementIO().write((SecurityRequirement) value);
        } else if (value instanceof SecurityScheme) {
            return securitySchemeIO().write((SecurityScheme) value);
        } else if (value instanceof Server) {
            return serverIO().write((Server) value);
        } else if (value instanceof ServerVariable) {
            return serverVariableIO().write((ServerVariable) value);
        } else if (value instanceof Tag) {
            return tagIO().write((Tag) value);
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
            setAllIfPresent(node, extensionIO().write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }

    /**
     * Checks whether a schema has only the {@code $ref} property set
     *
     * @param schema the schema to check
     * @return {@code true} if {@code schema} has one property and it's named {@code $ref}, otherwise {@code false}
     */
    private static boolean isSoloRef(Schema schema) {
        if (!(schema instanceof SchemaImpl)) {
            return false;
        }
        SchemaImpl s = (SchemaImpl) schema;
        Map<String, Object> data = s.getDataMap();
        return data.size() == 1 && data.containsKey(PROP_REF);
    }

    /**
     * Checks whether a schema has only the {@code type} property set with the value {@code [null]}
     *
     * @param schema the schema to check
     * @return {@code true} if {@code schema} has one property and it's named {@code type} and has value {@code [null]},
     *         otherwise {@code false}
     */
    private static boolean isSoloTypeNull(Schema schema) {
        if (!(schema instanceof SchemaImpl)) {
            return false;
        }
        SchemaImpl s = (SchemaImpl) schema;
        Map<String, Object> data = s.getDataMap();
        return data.size() == 1 && s.getType() != null && s.getType().equals(Collections.singletonList(SchemaType.NULL));
    }

    /**
     * Replacement field values which should be used when writing a Schema in 3.0 format.
     * <p>
     * All fields which may need to change value between 3.1 and 3.0 have an entry in here.
     * <p>
     * This is written by compute30ReplacementFields and read by populateSchemaObject30
     */
    private static class ReplacementFields {
        private SchemaType type;
        private Boolean nullable;
        private BigDecimal minimum;
        private Boolean exclusiveMinimum;
        private BigDecimal maximum;
        private Boolean exclusiveMaximum;
        private Object example;
        private String ref;
        private List<Schema> allOf;
        private List<Schema> anyOf;
        private List<Object> enumeration;
    }

}
