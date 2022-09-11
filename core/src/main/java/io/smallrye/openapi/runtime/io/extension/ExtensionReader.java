package io.smallrye.openapi.runtime.io.extension;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Extensible;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.Annotations;

/**
 * Reading the Extension annotation
 *
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#specificationExtensions">specificationExtensions</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExtensionReader {

    private ExtensionReader() {
    }

    /**
     * Reads an array of Extension annotations. The AnnotationValue in this case is
     * an array of Extension annotations. These must be read and converted into a Map.
     *
     * @param context the scanning context
     * @param annotationValue map of {@literal @}Extension annotations
     * @return Map of Objects
     */
    public static Map<String, Object> readExtensions(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.annotationsMap("@Extension");
        Map<String, Object> e = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String extName = Annotations.stringValue(annotation, ExtensionConstant.PROP_NAME);
            e.put(extName, readExtensionValue(context, extName, annotation));
        }
        return e;
    }

    /**
     * Reads a List of Extension annotations. These must be read and converted into a Map.
     *
     * @param context the scanning context
     * @param extensions List of {@literal @}Extension annotations
     * @return Map of Objects
     */
    public static Map<String, Object> readExtensions(final AnnotationScannerContext context,
            final List<AnnotationInstance> extensions) {
        IoLogging.logger.annotationsMap("@Extension");
        Map<String, Object> e = new LinkedHashMap<>();
        for (AnnotationInstance annotation : extensions) {
            String extName = Annotations.stringValue(annotation, ExtensionConstant.PROP_NAME);
            e.put(extName, readExtensionValue(context, extName, annotation));
        }
        return e;
    }

    public static Map<String, Object> readExtensions(final AnnotationScannerContext context,
            final AnnotationInstance extensible) {
        AnnotationInstance[] nestedExtensions = Annotations.value(extensible, "extensions");
        List<AnnotationInstance> extensions;

        if (nestedExtensions != null) {
            extensions = Arrays.asList(nestedExtensions);
        } else if (extensible.value("extensions") != null) {
            // Zero-length array
            extensions = Collections.emptyList();
        } else {
            AnnotationTarget target = extensible.target();
            // target may be null - checked by JandexUtil methods
            extensions = Annotations.getRepeatableAnnotation(target,
                    ExtensionConstant.DOTNAME_EXTENSION,
                    ExtensionConstant.DOTNAME_EXTENSIONS);
        }

        return extensions.isEmpty() ? null : readExtensions(context, extensions);
    }

    /**
     * Reads a single Extension annotation. If the value must be parsed (as indicated by the
     * 'parseValue' attribute of the annotation), the parsing is delegated to the extensions
     * currently set in the scanner. The default value will parse the string using Jackson.
     *
     * @param context the scanning context
     * @param name the name of the extension
     * @param annotationInstance {@literal @}Extension annotation
     * @return a Java representation of the 'value' property, either a String or parsed value
     *
     */
    public static Object readExtensionValue(final AnnotationScannerContext context, final String name,
            final AnnotationInstance annotationInstance) {
        IoLogging.logger.annotation("@Extension");
        String extValue = Annotations.stringValue(annotationInstance, ExtensionConstant.PROP_VALUE);
        boolean parseValue = Annotations.booleanValueWithDefault(annotationInstance,
                ExtensionConstant.PROP_PARSE_VALUE);
        Object parsedValue = extValue;
        if (parseValue) {
            for (AnnotationScannerExtension e : context.getExtensions()) {
                parsedValue = e.parseExtension(name, extValue);
                if (parsedValue != null) {
                    break;
                }
            }
        }
        return parsedValue;
    }

    /**
     * Reads model extensions.
     *
     * @param node the json object
     * @param model the model to read to
     */
    public static void readExtensions(final JsonNode node, final Extensible<?> model) {
        for (Iterator<String> iterator = node.fieldNames(); iterator.hasNext();) {
            String fieldName = iterator.next();
            if (fieldName.toLowerCase().startsWith(ExtensionConstant.EXTENSION_PROPERTY_PREFIX)) {
                Object value = readObject(node.get(fieldName));
                model.addExtension(fieldName, value);
            }
        }
    }

    // helper methods for scanners

    public static List<AnnotationInstance> getExtensionsAnnotations(final AnnotationTarget target) {
        return Annotations.getRepeatableAnnotation(target,
                ExtensionConstant.DOTNAME_EXTENSION,
                ExtensionConstant.DOTNAME_EXTENSIONS);
    }

    public static String getExtensionName(final AnnotationInstance annotation) {
        return Annotations.stringValue(annotation, ExtensionConstant.PROP_NAME);
    }
}
