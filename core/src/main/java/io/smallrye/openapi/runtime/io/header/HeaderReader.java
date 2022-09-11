package io.smallrye.openapi.runtime.io.header;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.headers.HeaderImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.Parameterizable;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.content.ContentReader;
import io.smallrye.openapi.runtime.io.example.ExampleReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.io.schema.SchemaReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.Annotations;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Header from annotations or json
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#headerObject">headerObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class HeaderReader {

    private HeaderReader() {
    }

    /**
     * Reads a map of Header annotations.
     *
     * @param context the scanning context
     * @param annotationValue map of {@literal @}Header annotations
     * @return Map of Header models
     */
    public static Map<String, Header> readHeaders(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.annotationsMap("@Header");
        Map<String, Header> headers = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = Annotations.stringValue(nested, Parameterizable.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                headers.put(name, readHeader(context, nested));
            }
        }
        return headers;
    }

    /**
     * Reads the {@link Header} OpenAPI nodes.
     *
     * @param node the json node
     * @return Map of Header models
     */
    public static Map<String, Header> readHeaders(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.logger.jsonNodeMap("Headers");
        Map<String, Header> headers = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            headers.put(fieldName, readHeader(childNode));
        }

        return headers;
    }

    /**
     * Reads a Header annotation into a model.
     *
     * @param annotationInstance the {@literal @}Header annotations
     * @return Header model
     */
    private static Header readHeader(final AnnotationScannerContext context, final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        IoLogging.logger.singleAnnotation("@Header");
        Header header = new HeaderImpl();
        header.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.HEADER));
        header.setDescription(Annotations.stringValue(annotationInstance, Parameterizable.PROP_DESCRIPTION));
        header.setSchema(SchemaFactory.readSchema(context, annotationInstance.value(Parameterizable.PROP_SCHEMA)));
        header.setRequired(Annotations.value(annotationInstance, Parameterizable.PROP_REQUIRED));
        header.setDeprecated(Annotations.value(annotationInstance, Parameterizable.PROP_DEPRECATED));
        header.setAllowEmptyValue(Annotations.value(annotationInstance, Parameterizable.PROP_ALLOW_EMPTY_VALUE));
        header.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
        return header;
    }

    /**
     * Reads a {@link Header} OpenAPI node.
     *
     * @param node
     */
    private static Header readHeader(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.logger.singleJsonNode("Header");
        Header header = new HeaderImpl();
        header.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        header.setDescription(JsonUtil.stringProperty(node, Parameterizable.PROP_DESCRIPTION));
        header.setSchema(SchemaReader.readSchema(node.get(Parameterizable.PROP_SCHEMA)));
        header.setRequired(JsonUtil.booleanProperty(node, Parameterizable.PROP_REQUIRED).orElse(null));
        header.setDeprecated(JsonUtil.booleanProperty(node, Parameterizable.PROP_DEPRECATED).orElse(null));
        header.setAllowEmptyValue(JsonUtil.booleanProperty(node, Parameterizable.PROP_ALLOW_EMPTY_VALUE).orElse(null));
        header.setStyle(readHeaderStyle(node.get(Parameterizable.PROP_STYLE)));
        header.setExplode(JsonUtil.booleanProperty(node, Parameterizable.PROP_EXPLODE).orElse(null));
        header.setExample(readObject(node.get(Parameterizable.PROP_EXAMPLE)));
        header.setExamples(ExampleReader.readExamples(node.get(Parameterizable.PROP_EXAMPLES)));
        header.setContent(ContentReader.readContent(node.get(Parameterizable.PROP_CONTENT)));
        ExtensionReader.readExtensions(node, header);
        return header;
    }

    /**
     * Reads a header style.
     *
     * @param node the json node
     * @return Header style enum
     */
    private static Header.Style readHeaderStyle(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return HEADER_STYLE_LOOKUP.get(node.asText());
    }

    private static final Map<String, Header.Style> HEADER_STYLE_LOOKUP = new LinkedHashMap<>();
    static {
        Header.Style[] headerStyleValues = Header.Style.values();
        for (Header.Style style : headerStyleValues) {
            HEADER_STYLE_LOOKUP.put(style.toString(), style);
        }
    }
}
