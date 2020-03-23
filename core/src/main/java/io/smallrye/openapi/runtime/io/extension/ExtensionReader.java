package io.smallrye.openapi.runtime.io.extension;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Extensible;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Extension annotation
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#specificationExtensions">specificationExtensions</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExtensionReader {
    private static final Logger LOG = Logger.getLogger(ExtensionReader.class);

    private ExtensionReader() {
    }

    /**
     * Reads an array of Extension annotations. The AnnotationValue in this case is
     * an array of Extension annotations. These must be read and converted into a Map.
     * 
     * @param context the scanning context
     * @param annotationValue map of {@literal @}Extention annotations
     * @return Map of Objects
     */
    public static Map<String, Object> readExtensions(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @Extention annotations");
        Map<String, Object> e = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String extName = JandexUtil.stringValue(annotation, ExtensionConstant.PROP_NAME);
            e.put(extName, readExtensionValue(context, extName, annotation));
        }
        return e;
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
        LOG.debug("Processing @Extention annotation");
        String extValue = JandexUtil.stringValue(annotationInstance, ExtensionConstant.PROP_VALUE);
        boolean parseValue = JandexUtil.booleanValueWithDefault(annotationInstance,
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
        return JandexUtil.getRepeatableAnnotation(target,
                ExtensionConstant.DOTNAME_EXTENSION,
                ExtensionConstant.DOTNAME_EXTENSIONS);
    }

    public static String getExtensionName(final AnnotationInstance annotation) {
        return JandexUtil.stringValue(annotation, ExtensionConstant.PROP_NAME);
    }

    public static boolean isExtensionField(String fieldName) {
        return fieldName.toLowerCase().startsWith(ExtensionConstant.EXTENSION_PROPERTY_PREFIX);
    }
}
