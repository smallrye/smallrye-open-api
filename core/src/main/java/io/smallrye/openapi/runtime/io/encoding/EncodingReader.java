package io.smallrye.openapi.runtime.io.encoding;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.Encoding.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.media.EncodingImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.header.HeaderReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Encoding object annotation and json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#encodingObject">encodingObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class EncodingReader {

    private EncodingReader() {
    }

    /**
     * Reads an array of Encoding annotations as a Map.
     * 
     * @param context the scanning context
     * @param annotationValue Map of {@literal @}Encoding annotations
     * @return Map of Encoding models
     */
    public static Map<String, Encoding> readEncodings(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.annotationsMap("@Encoding");
        Map<String, Encoding> encodings = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String name = JandexUtil.stringValue(annotation, EncodingConstant.PROP_NAME);
            if (name != null) {
                encodings.put(name, readEncoding(context, annotation));
            }
        }
        return encodings;
    }

    /**
     * Reads a map of {@link Encoding} OpenAPI nodes.
     * 
     * @param node the json node
     * @return Map of Encoding models
     */
    public static Map<String, Encoding> readEncodings(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.logger.jsonNodeMap("Encoding");
        Map<String, Encoding> encodings = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String name = fieldNames.next();
            encodings.put(name, readEncoding(node.get(name)));
        }
        return encodings;
    }

    /**
     * Reads a single Encoding annotation into a model.
     * 
     * @param context the scanning context
     * @param annotationInstance the {@literal @}Encoding annotation
     * @return Encoding model
     */
    private static Encoding readEncoding(final AnnotationScannerContext context, final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        IoLogging.logger.singleAnnotation("@Encoding");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(JandexUtil.stringValue(annotationInstance, EncodingConstant.PROP_CONTENT_TYPE));
        encoding.setStyle(JandexUtil.enumValue(annotationInstance, EncodingConstant.PROP_STYLE, Style.class));
        encoding.setExplode(JandexUtil.booleanValue(annotationInstance, EncodingConstant.PROP_EXPLODE).orElse(null));
        encoding.setAllowReserved(
                JandexUtil.booleanValue(annotationInstance, EncodingConstant.PROP_ALLOW_RESERVED).orElse(null));
        encoding.setHeaders(
                HeaderReader.readHeaders(context, annotationInstance.value(EncodingConstant.PROP_HEADERS)));
        return encoding;
    }

    /**
     * Reads a {@link Encoding} OpenAPI node.
     * 
     * @param node the json node
     * @return Encoding model
     */
    private static Encoding readEncoding(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.logger.singleJsonNode("Encoding");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(JsonUtil.stringProperty(node, EncodingConstant.PROP_CONTENT_TYPE));
        encoding.setHeaders(HeaderReader.readHeaders(node.get(EncodingConstant.PROP_HEADERS)));
        encoding.setStyle(readEncodingStyle(node.get(EncodingConstant.PROP_STYLE)));
        encoding.setExplode(JsonUtil.booleanProperty(node, EncodingConstant.PROP_EXPLODE).orElse(null));
        encoding.setAllowReserved(JsonUtil.booleanProperty(node, EncodingConstant.PROP_ALLOW_RESERVED).orElse(null));
        ExtensionReader.readExtensions(node, encoding);
        return encoding;
    }

    /**
     * Reads an encoding style.
     * 
     * @param node the json node
     * @return Style enum
     */
    private static Style readEncodingStyle(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return ENCODING_STYLE_LOOKUP.get(node.asText());
    }

    private static final Map<String, Style> ENCODING_STYLE_LOOKUP = new LinkedHashMap<>();
    static {
        Style[] encodingStyleValues = Style.values();
        for (Style style : encodingStyleValues) {
            ENCODING_STYLE_LOOKUP.put(style.toString(), style);
        }
    }
}
