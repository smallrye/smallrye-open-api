package io.smallrye.openapi.runtime.io;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.spi.JsonProvider;

import org.eclipse.microprofile.config.ConfigProvider;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.OpenApiRuntimeException;
import io.xlate.yamljson.Yaml;

class JakartaJsonIO implements JsonIO<JsonValue, JsonArray, JsonObject, JsonArrayBuilder, JsonObjectBuilder> {

    private final OpenApiConfig config;
    private final JsonProvider json;
    private final JsonReaderFactory jsonReaderFactory;
    private final JsonWriterFactory jsonWriterFactory;

    private final JsonReaderFactory yamlReaderFactory;
    private final JsonWriterFactory yamlWriterFactory;

    public JakartaJsonIO(OpenApiConfig config, JsonProvider jsonProvider, JsonProvider yamlProvider) {
        this.config = config;
        this.json = jsonProvider;
        this.jsonReaderFactory = jsonProvider.createReaderFactory(Collections.emptyMap());
        this.jsonWriterFactory = jsonProvider.createWriterFactory(Collections.emptyMap());

        LoaderOptions loaderOptions = new LoaderOptions();
        Optional.ofNullable(this.config.getMaximumStaticFileSize()).ifPresent(loaderOptions::setCodePointLimit);
        Map<String, Object> yamlReaderConfig = new HashMap<>();
        yamlReaderConfig.put(Yaml.Settings.LOAD_CONFIG, loaderOptions);
        this.yamlReaderFactory = yamlProvider.createReaderFactory(yamlReaderConfig);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setMaxSimpleKeyLength(1024);
        Map<String, Object> yamlWriterConfig = new HashMap<>();
        yamlWriterConfig.put(Yaml.Settings.DUMP_CONFIG, dumperOptions);
        yamlWriterConfig.put(Yaml.Settings.DUMP_MINIMIZE_QUOTES, true);
        this.yamlWriterFactory = yamlProvider.createWriterFactory(yamlWriterConfig);
    }

    public JakartaJsonIO(OpenApiConfig config) {
        this(config, JsonProvider.provider(), Yaml.provider());
    }

    public JakartaJsonIO() {
        this(OpenApiConfig.fromConfig(ConfigProvider.getConfig()), JsonProvider.provider(), Yaml.provider());
    }

    @Override
    public boolean isArray(JsonValue value) {
        return value instanceof JsonArray;
    }

    @Override
    public JsonArray asArray(JsonValue value) {
        return (JsonArray) value;
    }

    @Override
    public List<JsonValue> entries(JsonArray array) {
        return array;
    }

    @Override
    public boolean isObject(JsonValue value) {
        return value instanceof JsonObject;
    }

    @Override
    public JsonObject asObject(JsonValue value) {
        return (JsonObject) value;
    }

    @Override
    public boolean isBoolean(JsonValue value) {
        if (value == null) {
            return false;
        }
        ValueType type = value.getValueType();
        return type == ValueType.TRUE || type == ValueType.FALSE;
    }

    @Override
    public Boolean asBoolean(JsonValue value) {
        ValueType type = value.getValueType();
        if (type == ValueType.TRUE) {
            return Boolean.TRUE;
        } else if (type == ValueType.FALSE) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(JsonObject object, String key) {
        return object.containsKey(key);
    }

    @Override
    public Set<Entry<String, JsonValue>> properties(JsonObject object) {
        return object.entrySet();
    }

    @Override
    public boolean isString(JsonValue value) {
        return value != null && value.getValueType() == ValueType.STRING;
    }

    @Override
    public String asString(JsonValue value) {
        if (value == null) {
            return null;
        }
        switch (value.getValueType()) {
            case ARRAY:
            case OBJECT:
            case NULL:
                return null;
            case STRING:
                return ((JsonString) value).getString();
            default:
                return value.toString();
        }
    }

    @Override
    public Integer getJsonInt(JsonObject object, String key) {
        JsonValue value = object.get(key);
        return value != null && value.getValueType() == ValueType.NUMBER ? ((JsonNumber) value).intValue() : null;
    }

    @Override
    public String getJsonString(JsonObject object, String key) {
        return asString(object.get(key));
    }

    @Override
    public Boolean getJsonBoolean(JsonObject object, String key) {
        JsonValue value = object.get(key);
        if (value == null) {
            return null; // NOSONAR - no boolean value present to return
        }
        switch (value.getValueType()) {
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
            default:
                return null; // NOSONAR - no boolean value present to return
        }
    }

    @Override
    public BigDecimal getJsonBigDecimal(JsonObject object, String key) {
        JsonValue value = object.get(key);
        return value != null && value.getValueType() == ValueType.NUMBER ? ((JsonNumber) value).bigDecimalValue() : null;
    }

    @Override
    public JsonValue getValue(JsonObject object, String key) {
        return object.get(key);
    }

    @Override
    public Optional<JsonArray> getArray(JsonObject object, String key) {
        JsonValue value = object.get(key);
        return value != null && value.getValueType() == ValueType.ARRAY ? Optional.of(value.asJsonArray()) : Optional.empty();
    }

    @Override
    public Optional<JsonObject> getObject(JsonObject object, String key) {
        JsonValue value = object.get(key);
        return value != null && value.getValueType() == ValueType.OBJECT ? Optional.of(value.asJsonObject()) : Optional.empty();
    }

    @Override
    public JsonValue toJson(Object value, JsonValue defaultValue, PropertyMapper<JsonValue, JsonObjectBuilder> propertyMapper) {
        if (value instanceof String) {
            return json.createValue((String) value);
        } else if (value instanceof JsonValue) {
            return (JsonValue) value;
        } else if (value instanceof BigDecimal) {
            return json.createValue((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            return json.createValue((BigInteger) value);
        } else if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? JsonValue.TRUE : JsonValue.FALSE;
        } else if (value instanceof Double) {
            return json.createValue((Double) value);
        } else if (value instanceof Float) {
            return json.createValue((Float) value);
        } else if (value instanceof Short) {
            return json.createValue((short) value);
        } else if (value instanceof Integer) {
            return json.createValue((Integer) value);
        } else if (value instanceof Long) {
            return json.createValue((Long) value);
        } else if (value instanceof Character) {
            return json.createValue(((Character) value).toString());
        } else if (value instanceof List) {
            JsonArrayBuilder array = createArray();
            ((List<?>) value).stream()
                    .map(v -> toJson(v, JsonValue.NULL, propertyMapper))
                    .forEach(array::add);
            return array.build();
        } else if (value instanceof Map) {
            JsonObjectBuilder object = createObject();
            ((Map<?, ?>) value)
                    .forEach((key, obj) -> object.add(String.valueOf(key), toJson(obj, JsonValue.NULL, propertyMapper)));
            return object.build();
            // TODO: handler for BaseModels
        } else if (value instanceof Enum) {
            return json.createValue(value.toString());
        } else {
            return defaultValue;
        }
    }

    @Override
    public Object fromJson(JsonValue value) {
        if (value == null || value == JsonValue.NULL) {
            return null;
        }
        switch (value.getValueType()) {
            case NULL:
                return null;
            case NUMBER:
                return ((JsonNumber) value).numberValue();
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
            case STRING:
                return ((JsonString) value).getString();
            case ARRAY: {
                List<Object> items = new ArrayList<>();
                value.asJsonArray().forEach(entry -> items.add(fromJson(entry)));
                return items;
            }
            case OBJECT: {
                Map<String, Object> items = new LinkedHashMap<>();
                value.asJsonObject().forEach((key, property) -> items.put(key, fromJson(property)));
                return items;
            }
            default:
                return null;
        }
    }

    @Override
    public <T> T fromJson(JsonValue object, Class<T> desiredType) {
        ValueType type = object.getValueType();
        if (desiredType == String.class) {
            if (type == ValueType.STRING) {
                return (T) ((JsonString) object).getString();
            } else if (type == ValueType.NUMBER) {
                return (T) ((JsonNumber) object).numberValue().toString();
            } else if (type == ValueType.TRUE) {
                return (T) "true";
            } else if (type == ValueType.FALSE) {
                return (T) "false";
            }
        }
        if (type == ValueType.NUMBER) {
            JsonNumber number = (JsonNumber) object;
            try {
                if (desiredType == Integer.class) {
                    return (T) Integer.valueOf(number.intValueExact());
                } else if (desiredType == Long.class) {
                    return (T) Long.valueOf(number.longValueExact());
                } else if (desiredType == BigInteger.class) {
                    return (T) number.bigIntegerValueExact();
                } else if (desiredType == BigDecimal.class) {
                    return (T) number.bigDecimalValue();
                }
            } catch (ArithmeticException e) {
                // thrown if conversion cannot be done losslessly
                // fall through to return null later
            }
        }
        if (desiredType == Boolean.class) {
            if (type == ValueType.TRUE) {
                return (T) Boolean.TRUE;
            } else if (type == ValueType.FALSE) {
                return (T) Boolean.FALSE;
            }
        }
        // Nothing matched
        return null;
    }

    @Override
    public String toString(JsonValue value, Format format) {
        Writer output = new StringWriter();

        try (JsonWriter writer = format == Format.JSON ? jsonWriterFactory.createWriter(output)
                : yamlWriterFactory.createWriter(output)) {
            writer.write(value);
        } catch (JsonException e) {
            throw new OpenApiRuntimeException("Failed to read " + format + " stream", e);
        }

        return output.toString();
    }

    @Override
    public JsonValue fromReader(Reader reader, Format format) {
        try (JsonReader jsonReader = format == Format.JSON ? jsonReaderFactory.createReader(reader)
                : yamlReaderFactory.createReader(reader)) {
            return jsonReader.readValue();
        } catch (JsonException e) {
            throw new OpenApiRuntimeException("Failed to read " + format + " stream", e);
        }
    }

    @Override
    public JsonArrayBuilder createArray() {
        return json.createArrayBuilder();
    }

    @Override
    public void add(JsonArrayBuilder array, JsonValue value) {
        array.add(value);
    }

    @Override
    public JsonArray buildArray(JsonArrayBuilder array) {
        return array.build();
    }

    @Override
    public JsonObjectBuilder createObject() {
        return json.createObjectBuilder();
    }

    @Override
    public void set(JsonObjectBuilder object, String key, JsonValue value) {
        object.add(key, value);
    }

    @Override
    public void setAll(JsonObjectBuilder object, JsonObject valueSource) {
        valueSource.forEach(object::add);
    }

    @Override
    public JsonObject buildObject(JsonObjectBuilder object) {
        return object.build();
    }

    @Override
    public JsonValue nullValue() {
        return JsonValue.NULL;
    }
}
