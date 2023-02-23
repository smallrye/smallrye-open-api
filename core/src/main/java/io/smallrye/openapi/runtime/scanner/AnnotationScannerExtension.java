package io.smallrye.openapi.runtime.scanner;

import java.util.Collection;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;

/**
 * Extension point for supporting extensions to OpenAPI Scanners.
 * Implement this directly
 */
public interface AnnotationScannerExtension {

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
    default Object parseValue(String value) {
        return JsonUtil.parseValue(value);
    }
}
