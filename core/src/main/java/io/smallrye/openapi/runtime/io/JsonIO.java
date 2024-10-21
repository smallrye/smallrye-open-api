package io.smallrye.openapi.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.smallrye.openapi.api.OpenApiConfig;

/**
 * Abstraction layer around a library for reading and writing JSON. (E.g. Jakarta JSON-P or Jackson).
 *
 * @param <V> the type for a JSON value
 * @param <A> the type for a JSON array
 * @param <O> the type of a JSON object
 * @param <AB> the type used to build a JSON array
 * @param <OB> the type used to build a JSON object
 */
public interface JsonIO<V, A extends V, O extends V, AB, OB> {

    static PropertyMapper<?, ?> NOOP = new PropertyMapper<>() {
    };

    public interface PropertyMapper<V, OB> {
        /**
         * Optionally convert the entire object to a JSON value. If no value mapping
         * should occur, implementations should return an empty Optional.
         *
         * @param object model object that may be mapped to a JSON value
         * @return an optional JSON value that is mapped from the object
         */
        default Optional<V> mapObject(Object object) {
            return Optional.empty();
        }

        /**
         * Optionally convert the property with given name and value to a JSON value.
         * If no value mapping should occur, implementations should return an empty Optional.
         */
        default Optional<V> mapProperty(Object object, String propertyName, Object propertyValue) {
            return Optional.empty();
        }

        /**
         * Map any additional properties from the given model object to the nodeBuilder that
         * will be the resulting JSON value.
         */
        default void mapObject(Object object, OB nodeBuilder) {
        }
    }

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

    /**
     * Check whether a JSON value is an array
     *
     * @param value the JSON value to check
     * @return {@code true} if {@code value} is a JSON array, otherwise {@code false}
     */
    boolean isArray(V value);

    /**
     * Cast a JSON value to a JSON array
     *
     * @param value the JSON value, which must represent a JSON array
     * @return the JSON array
     * @throws ClassCastException if {@code value} is not a JSON array
     */
    A asArray(V value);

    /**
     * Get the list of JSON values contained by a JSON array
     *
     * @param array the JSON array
     * @return the list of JSON values in the array
     */
    List<V> entries(A array);

    /**
     * Check whether a JSON value is a JSON object
     *
     * @param value the JSON value to check
     * @return {@code true} if {@code value} is a JSON object, otherwise {@code false}
     */
    boolean isObject(V value);

    /**
     * Cast a JSON value to a JSON object
     *
     * @param value the JSON value, which must represent a JSON object
     * @return the JSON object
     * @throws ClassCastException if {@code value} is not a JSON object
     */
    O asObject(V value);

    /**
     * Check whether a JSON object contains a particular key
     *
     * @param object the JSON object to check
     * @param key the key to look for
     * @return {@code true} if {@code object} contains key {@code key}, otherwise {@code false}
     */
    boolean hasKey(O object, String key);

    /**
     * Get all the properties (keys and values) of a JSON object.
     * <p>
     * This is a similar operation to {@link Map#entrySet()}.
     *
     * @param object the JSON object
     * @return the set of key-value pairs
     */
    Set<Map.Entry<String, V>> properties(O object);

    /**
     * Check whether a JSON value is a JSON string.
     *
     * @param value the JSON value to check
     * @return {@code true} if {@code value} is a string, otherwise {@code false}
     */
    boolean isString(V value);

    /**
     * Convert a JSON string, number or boolean to a {@code String}.
     *
     * @param value the JSON value, must be a string
     * @return the String value, or {@code null} if the JSON value does not represent a string, number or boolean
     */
    String asString(V value);

    /**
     * Check whether a JSON value is a JSON boolean.
     *
     * @param value the JSON value to check
     * @return {@code true} if {@code value} is a boolean, otherwise {@code false}
     */
    boolean isBoolean(V value);

    /**
     * Convert a JSON boolean to a {@code Boolean}.
     *
     * @param value the JSON value, must be a boolean
     * @return the Boolean value, or {@code null} if the JSON value does not represent a boolean
     */
    Boolean asBoolean(V value);

    /**
     * Get a property from a JSON object as a string. The same conversions are performed as {@link #asString(Object)}.
     *
     * @param object the JSON object
     * @param key the property key
     * @return the property value, or {@code null} if {@code object} is not a JSON object, the property is not present, or the
     *         property value is not a string, number or boolean
     */
    default String getString(V object, String key) {
        if (isObject(object)) {
            return getJsonString(asObject(object), key);
        }
        return null;
    }

    /**
     * Get an integer property from a JSON object
     *
     * @param object the JSON object
     * @param key the property key
     * @return the property value, or {@code null} if {@code object} is not a JSON object, the property is not present, or the
     *         property value is not an integer
     */
    default Integer getInt(V object, String key) {
        if (isObject(object)) {
            return getJsonInt(asObject(object), key);
        }
        return null;
    }

    /**
     * Get an boolean property from a JSON object
     *
     * @param object the JSON object
     * @param key the property key
     * @return the property value, or {@code null} if {@code object} is not a JSON object, the property is not present, or the
     *         property value is not a boolean
     */
    default Boolean getBoolean(V object, String key) {
        if (isObject(object)) {
            return getJsonBoolean(asObject(object), key);
        }
        return null; // NOSONAR
    }

    /**
     * Get an number property from a JSON object
     *
     * @param object the JSON object
     * @param key the property key
     * @return the property value, or {@code null} if {@code object} is not a JSON object, the property is not present, or the
     *         property value is not a number
     */
    default BigDecimal getBigDecimal(V object, String key) {
        if (isObject(object)) {
            return getJsonBigDecimal(asObject(object), key);
        }
        return null;
    }

    /**
     * Get an integer property from a JSON object
     *
     * @param object the JSON object
     * @param key the property key
     * @return the property value, or {@code null} if the property is not present or is not an integer
     */
    Integer getJsonInt(O object, String key);

    /**
     * Get a property from a JSON object as a string. The same conversions are performed as {@link #asString(Object)}.
     *
     * @param object the JSON object
     * @param key the property key
     * @return the property value, or {@code null} if the property is not present or is not a string, number or boolean
     */
    String getJsonString(O object, String key);

    /**
     * Get an boolean property from a JSON object
     *
     * @param object the JSON object
     * @param key the property key
     * @return the property value, or {@code null} if the property is not present or is not a boolean
     */
    Boolean getJsonBoolean(O object, String key);

    /**
     * Get an number property from a JSON object
     *
     * @param object the JSON object
     * @param key the property key
     * @return the property value, or {@code null} if the property is not present or is not a number
     */
    BigDecimal getJsonBigDecimal(O object, String key);

    /**
     * Get a property from a JSON object
     *
     * @param object the JSON object
     * @param key the property key
     * @return the property value, or {@code null} if the property is not present
     */
    V getValue(O object, String key);

    /**
     * Get an JSON array property from a JSON object
     *
     * @param object the JSON object
     * @param key the property key
     * @return an {@code Optional} containing the property value, or an empty {@code Optional} if the property is not present,
     *         or the property is not an array
     */
    Optional<A> getArray(O object, String key);

    /**
     * Get an JSON array property from a JSON object and convert it into a list of Java objects
     *
     * @param <T> the type that each value in the JSON array should be converted into
     * @param object the JSON object
     * @param key the property key
     * @param valueMapper a function to convert a value within the JSON array into a Java object
     * @return an {@code Optional} containing the list of Java objects, or an empty {@code Optional} if the property is not
     *         present, or the property is not an array
     */
    default <T> Optional<List<T>> getArray(O object, String key, Function<V, T> valueMapper) {
        return getArray(object, key)
                .map(this::entries)
                .map(entries -> entries.stream().map(valueMapper).collect(Collectors.toList()));
    }

    /**
     * Get an JSON object property from a JSON object
     *
     * @param object the JSON object
     * @param key the property key
     * @return an {@code Optional} containing the property value, or an empty {@code Optional} if the property is not present,
     *         or the property is not an object
     */
    Optional<O> getObject(O object, String key);

    /**
     * Convert a Java object to JSON. See {@link #toJson(Object, Object, PropertyMapper)} for the list of supported types
     *
     * @param object the JSON object
     * @return an {@code Optional} containing the JSON value, or an empty {@code Optional} if {@code object} is not one of the
     *         supported types
     */
    @SuppressWarnings("unchecked")
    default Optional<V> toJson(Object object) {
        return Optional.ofNullable(toJson(object, null, (PropertyMapper<V, OB>) NOOP));
    }

    default Optional<V> toJson(Object object, PropertyMapper<V, OB> handler) {
        return Optional.ofNullable(toJson(object, null, handler));
    }

    /**
     * Convert a JSON value into a Java object.
     *
     * @param object the JSON value
     * @return the Java object, which may be {@code null} if {@code object} is {@code null} or represents a JSON null value
     */
    Object fromJson(V object);

    /**
     * Convert a basic JSON value into a Java object of the desired type. This method cannot be used to convert JSON arrays or
     * objects.
     * <p>
     * The supported values for {@code desiredType} are:
     * <ul>
     * <li>{@code String}
     * <li>{@code Integer}
     * <li>{@code BigInteger}
     * <li>{@code Long}
     * <li>{@code BigDecimal}
     * <li>{@code Boolean}
     * </ul>
     *
     * @param <T> the desired Java type
     * @param object the JSON object
     * @param desiredType the desired Java type
     * @return the JSON object, or {@code null} if {@code object} cannot be converted to the desired type
     */
    <T> T fromJson(V object, Class<T> desiredType);

    /**
     * Convert a Java object to JSON.
     * <p>
     * The following types are supported:
     * <ul>
     * <li>{@code V} (the JSON value type)
     * <li>{@code String}
     * <li>{@code BigDecimal}
     * <li>{@code BigInteger}
     * <li>{@code Boolean}
     * <li>{@code Double}
     * <li>{@code Float}
     * <li>{@code Short}
     * <li>{@code Integer}
     * <li>{@code Long}
     * <li>{@code Character}
     * <li>{@code Enum}
     * <li>{@code List} where each item is a supported type
     * <li>{@code Map} where each key is a {@code String} and each value is a supported type
     * </ul>
     *
     * @param object the JSON object
     * @param defaultValue the default value to return if {@code value} cannot be converted to JSON
     * @param propertyMapper mapper object to alter the default mapping of the object and its properties to JSON
     * @return the JSON value, or {@code defaultValue} if {@code value} cannot be converted to JSON
     */
    V toJson(Object object, V defaultValue, PropertyMapper<V, OB> propertyMapper);

    /**
     * Read a JSON or YAML document from a {@code String}
     *
     * @param value the JSON or YAML document
     * @param format the format
     * @return the root JSON value from the document
     */
    default V fromString(String value, Format format) {
        try (Reader reader = new StringReader(value)) {
            return fromReader(reader, format);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read a JSON or YAML document from an {@code InputStream}. The stream is read using the {@link Charset#defaultCharset()
     * default charset}.
     *
     * @param stream the input stream to read the JSON or YAML document from
     * @param format the format
     * @return the root JSON value from the document
     */
    default V fromStream(InputStream stream, Format format) {
        try (Reader reader = new InputStreamReader(stream)) {
            return fromReader(reader, format);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read a YAML document from a {@code Reader}.
     *
     * @param reader the reader to read the YAML document from
     * @return the root JSON value from the document
     */
    default V fromReader(Reader reader) throws IOException {
        return fromReader(reader, Format.YAML);
    }

    /**
     * Read a JSON or YAML document from a {@code Reader}.
     *
     * @param reader the reader to read the JSON or YAML document from
     * @param format the format
     * @return the root JSON value from the document
     */
    V fromReader(Reader reader, Format format) throws IOException;

    /**
     * Serialize a JSON value to a JSON or YAML string
     *
     * @param object the JSON value
     * @param format the desired format
     * @return the serialization of {@code object}
     */
    String toString(V object, Format format);

    /**
     * Create a JSON array builder which can be used to build a JSON array.
     *
     * Example:
     *
     * <pre>{@code
     * AB builder = jsonIO().createArray();
     * jsonIO().add(builder, value1);
     * jsonIO().add(builder, value2);
     * A array = jsonIO().buildArray(builder);
     * }</pre>
     *
     * @return the JSON array builder
     */
    AB createArray();

    /**
     * Add a JSON value to a JSON array builder.
     *
     * @param array the array builder
     * @param value the value to add
     * @see #createArray()
     */
    void add(AB array, V value);

    /**
     * Convert a JSON array builder into a JSON array.
     *
     * @param array the JSON array builder
     * @return the JSON array
     * @see #createArray()
     */
    A buildArray(AB array);

    /**
     * Create a JSON object builder which can be used to build a JSON object.
     *
     * Example:
     *
     * <pre>{@code
     * OB builder = jsonIO().createObject();
     * jsonIO().set(builder, "key1", value1);
     * jsonIO().set(builder, "key2", value2);
     * O object = jsonIO().buildObject(builder);
     * }</pre>
     *
     * @return the JSON object builder
     */
    OB createObject();

    /**
     * Set a property on a JSON object builder
     *
     * @param object the JSON object builder
     * @param key the property key
     * @param value the property value
     * @see #createObject()
     */
    void set(OB object, String key, V value);

    /**
     * Copy all properties from a JSON object to a JSON object builder.
     *
     * @param object the JSON object builder
     * @param valueSource the JSON object
     * @see #createObject()
     */
    void setAll(OB object, O valueSource);

    /**
     * Convert a JSON object builder into a JSON object.
     *
     * @param object the JSON object builder
     * @return the JSON object
     * @see #createObject()
     */
    O buildObject(OB object);

    /**
     * Returns the JSON value representing {@code null}.
     */
    V nullValue();
}
