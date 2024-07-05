package io.smallrye.openapi.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.smallrye.openapi.api.OpenApiConfig;

public interface JsonIO<V, A extends V, O extends V, AB, OB> {

    public static <V, A extends V, O extends V, AB, OB> JsonIO<V, A, O, AB, OB> newInstance(OpenApiConfig config) {
        @SuppressWarnings("unchecked")
        JsonIO<V, A, O, AB, OB> jackson = (JsonIO<V, A, O, AB, OB>) new JacksonJsonIO(config);
        return jackson;
    }

    private boolean wrapped(String value, String prefix, String suffix) {
        return value.startsWith(prefix) && value.endsWith(suffix);
    }

    default Object parseValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String trimmedValue = value.trim();

        switch (trimmedValue.charAt(0)) {
            case '{':
            case '[':
                if (wrapped(trimmedValue, "{", "}") || wrapped(trimmedValue, "[", "]")) {
                    /* Looks like a JSON Object or Array */
                    try {
                        return fromJson(fromString(trimmedValue, Format.JSON));
                    } catch (Exception e) {
                        IoLogging.logger.unparseableJson(trimmedValue);
                    }
                }
                break;
            case '-': /* Negative Number */
            case '0': /* Numbers */
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                try {
                    return new BigDecimal(trimmedValue);
                } catch (NumberFormatException e) {
                    IoLogging.logger.unparseableJson(trimmedValue);
                }
                break;
            case 't':
                return "true".equals(trimmedValue) ? Boolean.TRUE : value;
            case 'f':
                return "false".equals(trimmedValue) ? Boolean.FALSE : value;
            default:
                break;
        }

        // JSON String
        return value;
    }

    boolean isArray(V value);

    A asArray(V value);

    List<V> entries(A array);

    boolean isObject(V value);

    O asObject(V value);

    boolean hasKey(O object, String key);

    Set<Map.Entry<String, V>> properties(O object);

    boolean isString(V value);

    String asString(V value);

    default String getString(V object, String key) {
        if (isObject(object)) {
            return getJsonString(asObject(object), key);
        }
        return null;
    }

    default Integer getInt(V object, String key) {
        if (isObject(object)) {
            return getJsonInt(asObject(object), key);
        }
        return null;
    }

    default Boolean getBoolean(V object, String key) {
        if (isObject(object)) {
            return getJsonBoolean(asObject(object), key);
        }
        return null; // NOSONAR
    }

    default BigDecimal getBigDecimal(V object, String key) {
        if (isObject(object)) {
            return getJsonBigDecimal(asObject(object), key);
        }
        return null;
    }

    Integer getJsonInt(O object, String key);

    String getJsonString(O object, String key);

    Boolean getJsonBoolean(O object, String key);

    BigDecimal getJsonBigDecimal(O object, String key);

    V getValue(O object, String key);

    Optional<A> getArray(O object, String key);

    default <T> Optional<List<T>> getArray(O object, String key, Function<V, T> valueMapper) {
        return getArray(object, key)
                .map(this::entries)
                .map(entries -> entries.stream().map(valueMapper).collect(Collectors.toList()));
    }

    Optional<O> getObject(O object, String key);

    default Optional<V> toJson(Object object) {
        return Optional.ofNullable(toJson(object, null));
    }

    Object fromJson(V object);

    V toJson(Object object, V defaultValue);

    default V fromString(String value, Format format) {
        try (Reader reader = new StringReader(value)) {
            return fromReader(reader, format);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default V fromStream(InputStream stream, Format format) {
        try (Reader reader = new InputStreamReader(stream)) {
            return fromReader(reader, format);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default V fromReader(Reader reader) throws IOException {
        return fromReader(reader, Format.YAML);
    }

    V fromReader(Reader reader, Format format) throws IOException;

    String toString(V object, Format format);

    AB createArray();

    void add(AB array, V value);

    A buildArray(AB array);

    OB createObject();

    void set(OB object, String key, V value);

    void setAll(OB object, O valueSource);

    O buildObject(OB object);
}
