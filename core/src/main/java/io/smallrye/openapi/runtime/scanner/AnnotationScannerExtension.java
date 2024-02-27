package io.smallrye.openapi.runtime.scanner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.JsonIO;
import io.smallrye.openapi.runtime.io.media.SchemaIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;

/**
 * Extension point for supporting extensions to OpenAPI Scanners.
 * Implement this directly
 */
public interface AnnotationScannerExtension {

    public static List<AnnotationScannerExtension> defaultExtension() {
        return Collections.singletonList(new Default<>());
    }

    static class Default<V> implements AnnotationScannerExtension {
        JsonIO<V, ?, ?, ?, ?> jackson = JsonIO.newInstance(null);

        @Override
        public Object parseValue(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }

            value = value.trim();

            if ("true".equals(value) || "false".equals(value)) {
                return Boolean.valueOf(value);
            }

            switch (value.charAt(0)) {
                case '{': /* JSON Object */
                case '[': /* JSON Array */
                case '-': /* JSON Negative Number */
                case '0': /* JSON Numbers */
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
                        return jackson.fromJson(jackson.fromString(value, Format.JSON));
                    } catch (Exception e) {
                        // TODO log the error
                        break;
                    }
                default:
                    break;
            }

            // JSON String
            return value;
        }

        @Override
        public Schema parseSchema(String jsonSchema) {
            return new SchemaIO<>(IOContext.forJson(jackson))
                    .readValue(jackson.fromString(jsonSchema, Format.JSON));
        }
    }

    /**
     * Unwraps an asynchronous type such as
     * <code>CompletionStage&lt;X&gt;</code> into its resolved type
     * <code>X</code>
     *
     * @param type
     *        the type to unwrap if it is a supported async type
     * @return the resolved type or null if not supported
     */
    default Type resolveAsyncType(Type type) {
        return null;
    }

    /**
     * Gives a chance to extensions to process the set of scanner application classes.
     *
     * @param scanner the scanner used for application scanning
     * @param applications the set of rest application classes
     */
    default void processScannerApplications(AnnotationScanner scanner, Collection<ClassInfo> applications) {
    }

    /**
     * Returns true if the given annotation is a scanner annotation extension,
     * such as would be in the scanner's package.
     *
     *
     * @param instance the annotation to check
     * @return true if the given annotation is a jax-rs annotation extension
     */
    default boolean isScannerAnnotationExtension(AnnotationInstance instance) {
        return false;
    }

    /**
     * Parses an OpenAPI Extension value. The value may be:
     *
     * - JSON object - starts with '{'
     * - JSON array - starts with '['
     * - number
     * - boolean
     * - string
     *
     * @param key the name of the extension property
     * @param value the string value of the extension
     * @return the extension
     */
    default Object parseExtension(String key, String value) {
        return parseValue(value);
    }

    /**
     * Parse a string value as an Object.
     * By default, try to parse the value from JSON
     * The value may be:
     *
     * - JSON object - starts with '{'
     * - JSON array - starts with '['
     * - number
     * - boolean
     * - string
     *
     * @param value the string value
     * @return the parsed value as Object
     */
    Object parseValue(String value);

    /**
     * Parse a string value as a Schema
     *
     * @param jsonSchema the string value of the schema, in JSON format
     * @return the parsed value as Schema
     */
    Schema parseSchema(String jsonSchema);
}
