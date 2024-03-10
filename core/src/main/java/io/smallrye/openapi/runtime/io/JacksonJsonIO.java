package io.smallrye.openapi.runtime.io;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.ConfigProvider;
import org.yaml.snakeyaml.LoaderOptions;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.OpenApiRuntimeException;

class JacksonJsonIO implements JsonIO<JsonNode, ArrayNode, ObjectNode, ArrayNode, ObjectNode> {

    private static final JsonNodeFactory factory = JsonNodeFactory.instance;

    private final OpenApiConfig config;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final ObjectWriter jsonWriter;
    private final ObjectWriter yamlWriter;

    public JacksonJsonIO(OpenApiConfig config, ObjectMapper objectMapper) {
        this.config = config != null ? config : OpenApiConfig.fromConfig(ConfigProvider.getConfig());
        this.jsonMapper = objectMapper;
        this.jsonWriter = objectMapper.writerWithDefaultPrettyPrinter();

        LoaderOptions loaderOptions = new LoaderOptions();
        Optional.ofNullable(this.config.getMaximumStaticFileSize()).ifPresent(loaderOptions::setCodePointLimit);

        JsonFactory yamlFactory = new YAMLFactoryBuilder(new YAMLFactory())
                .loaderOptions(loaderOptions)
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
                .enable(YAMLGenerator.Feature.ALLOW_LONG_KEYS)
                .build();

        this.yamlMapper = jsonMapper.copyWith(yamlFactory);
        this.yamlWriter = yamlMapper.writer().with(yamlFactory);
    }

    public JacksonJsonIO(OpenApiConfig config) {
        this(config, new ObjectMapper());
    }

    public JacksonJsonIO(ObjectMapper objectMapper) {
        this(null, objectMapper);
    }

    public JacksonJsonIO() {
        this(null, new ObjectMapper());
    }

    @Override
    public boolean isArray(JsonNode value) {
        return value instanceof ArrayNode;
    }

    @Override
    public ArrayNode asArray(JsonNode value) {
        return (ArrayNode) value;
    }

    @Override
    public List<JsonNode> entries(ArrayNode array) {
        List<JsonNode> entries = new ArrayList<>(array.size());
        array.forEach(entries::add);
        return entries;
    }

    @Override
    public boolean isObject(JsonNode value) {
        return value instanceof ObjectNode;
    }

    @Override
    public ObjectNode asObject(JsonNode value) {
        return (ObjectNode) value;
    }

    @Override
    public boolean hasKey(ObjectNode object, String key) {
        return object.has(key);
    }

    @Override
    public Set<Entry<String, JsonNode>> properties(ObjectNode object) {
        return object.properties();
    }

    @Override
    public boolean isString(JsonNode value) {
        return value != null && value.isTextual();
    }

    @Override
    public String asString(JsonNode value) {
        return value != null && value.isValueNode() ? value.asText() : null;
    }

    @Override
    public Integer getJsonInt(ObjectNode object, String key) {
        JsonNode value = object.get(key);
        return value != null && value.isInt() ? value.asInt() : null;
    }

    @Override
    public String getJsonString(ObjectNode object, String key) {
        return asString(object.get(key));
    }

    @Override
    public Boolean getJsonBoolean(ObjectNode object, String key) {
        JsonNode value = object.get(key);
        return value != null && value.isBoolean() ? value.asBoolean() : null;
    }

    @Override
    public BigDecimal getJsonBigDecimal(ObjectNode object, String key) {
        JsonNode value = object.get(key);
        return value != null ? new BigDecimal(value.asText()) : null;
    }

    @Override
    public JsonNode getValue(ObjectNode object, String key) {
        return object.get(key);
    }

    @Override
    public Optional<ArrayNode> getArray(ObjectNode object, String key) {
        JsonNode value = object.get(key);
        return value != null && value.isArray() ? Optional.of((ArrayNode) value) : Optional.empty();
    }

    @Override
    public Optional<ObjectNode> getObject(ObjectNode object, String key) {
        JsonNode value = object.get(key);
        return value != null && value.isObject() ? Optional.of((ObjectNode) value) : Optional.empty();
    }

    @Override
    public ArrayNode createArray() {
        return factory.arrayNode();
    }

    @Override
    public void add(ArrayNode array, JsonNode value) {
        array.add(value);
    }

    @Override
    public ArrayNode buildArray(ArrayNode array) {
        return array;
    }

    @Override
    public ObjectNode createObject() {
        return factory.objectNode();
    }

    @Override
    public void set(ObjectNode object, String key, JsonNode value) {
        object.set(key, value);
    }

    @Override
    public void setAll(ObjectNode object, ObjectNode valueSource) {
        object.setAll(valueSource);
    }

    @Override
    public ObjectNode buildObject(ObjectNode object) {
        return object;
    }

    @Override
    public JsonNode toJson(Object value, JsonNode defaultValue) {
        if (value instanceof String) {
            return factory.textNode((String) value);
        } else if (value instanceof JsonNode) {
            return (JsonNode) value;
        } else if (value instanceof BigDecimal) {
            return factory.numberNode((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            return factory.numberNode((BigInteger) value);
        } else if (value instanceof Boolean) {
            return factory.booleanNode((Boolean) value);
        } else if (value instanceof Double) {
            return factory.numberNode((Double) value);
        } else if (value instanceof Float) {
            return factory.numberNode((Float) value);
        } else if (value instanceof Integer) {
            return factory.numberNode((Integer) value);
        } else if (value instanceof Long) {
            return factory.numberNode((Long) value);
        } else if (value instanceof List) {
            ArrayNode array = createArray();
            ((List<?>) value).stream()
                    .map(v -> toJson(v, factory.nullNode()))
                    .forEach(array::add);
            return array;
        } else if (value instanceof Map) {
            ObjectNode object = createObject();
            ((Map<?, ?>) value).forEach((key, obj) -> object.set(String.valueOf(key), toJson(obj, factory.nullNode())));
            return object;
        } else if (value instanceof Enum) {
            return factory.textNode(value.toString());
        } else {
            return defaultValue;
        }
    }

    @Override
    public Object fromJson(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isBigDecimal()) {
            return new BigDecimal(value.asText());
        }
        if (value.isBigInteger()) {
            return new BigInteger(value.asText());
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isDouble()) {
            return value.asDouble();
        }
        if (value.isFloat()) {
            return value.asDouble();
        }
        if (value.isInt()) {
            return value.asInt();
        }
        if (value.isLong()) {
            return value.asLong();
        }
        if (value.isTextual()) {
            return value.asText();
        }
        if (value.isArray()) {
            List<Object> items = new ArrayList<>();
            value.elements().forEachRemaining(entry -> items.add(fromJson(entry)));
            return items;
        }
        if (value.isObject()) {
            Map<String, Object> items = new LinkedHashMap<>();
            value.properties().forEach(field -> items.put(field.getKey(), fromJson(field.getValue())));
            return items;
        }
        return null;
    }

    @Override
    public String toString(JsonNode value, Format format) {
        try {
            if (format == Format.JSON) {
                return jsonWriter.writeValueAsString(value);
            } else {
                return yamlWriter.writeValueAsString(value);
            }
        } catch (IOException e) {
            throw new OpenApiRuntimeException(e);
        }
    }

    @Override
    public JsonNode fromReader(Reader reader, Format format) {
        try {
            if (format == Format.JSON) {
                return jsonMapper.readTree(reader);
            } else {
                return yamlMapper.readTree(reader);
            }
        } catch (IOException e) {
            throw new OpenApiRuntimeException("Failed to read " + format + " stream", e);
        }
    }
}
