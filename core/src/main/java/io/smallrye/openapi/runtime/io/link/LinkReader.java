package io.smallrye.openapi.runtime.io.link;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.links.Link;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.links.LinkImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.server.ServerReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Link annotation and json
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#linkObject">linkObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class LinkReader {

    private LinkReader() {
    }

    /**
     * Reads Link annotations
     *
     * @param annotationValue map of {@literal @}Link annotations
     * @return Map of Link model
     */
    public static Map<String, Link> readLinks(final AnnotationScannerContext context, final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.annotationsMap("@Link");
        Map<String, Link> links = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = context.annotations().value(nested, LinkConstant.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                links.put(name, readLink(context, nested));
            }
        }
        return links;
    }

    /**
     * Reads the {@link Link} OpenAPI nodes.
     *
     * @param node the json node
     * @return Map of Link model
     */
    public static Map<String, Link> readLinks(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.logger.jsonNodeMap("Link");
        Map<String, Link> links = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            links.put(fieldName, readLink(childNode));
        }

        return links;
    }

    /**
     * Reads a Link annotation into a model.
     *
     * @param annotationInstance {@literal @}Link annotation
     * @return Link model
     */
    private static Link readLink(final AnnotationScannerContext context, final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        IoLogging.logger.singleAnnotation("@Link");
        Link link = new LinkImpl();
        link.setOperationRef(context.annotations().value(annotationInstance, LinkConstant.PROP_OPERATION_REF));
        link.setOperationId(context.annotations().value(annotationInstance, LinkConstant.PROP_OPERATION_ID));
        link.setParameters(readLinkParameters(context, annotationInstance.value(LinkConstant.PROP_PARAMETERS)));
        link.setDescription(context.annotations().value(annotationInstance, LinkConstant.PROP_DESCRIPTION));
        link.setRequestBody(context.annotations().value(annotationInstance, LinkConstant.PROP_REQUEST_BODY));
        link.setServer(ServerReader.readServer(context, annotationInstance.value(LinkConstant.PROP_SERVER)));
        link.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.LINK));
        link.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
        return link;
    }

    /**
     * Reads a {@link Link} OpenAPI node.
     *
     * @param node the json node
     * @return Link model
     */
    private static Link readLink(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.logger.singleJsonNode("Link");
        Link link = new LinkImpl();
        link.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));

        link.setOperationRef(JsonUtil.stringProperty(node, LinkConstant.PROP_OPERATION_REF));
        link.setOperationId(JsonUtil.stringProperty(node, LinkConstant.PROP_OPERATION_ID));
        link.setParameters(readLinkParameters(node.get(LinkConstant.PROP_PARAMETERS)));
        link.setRequestBody(readObject(node.get(LinkConstant.PROP_REQUEST_BODY)));
        link.setDescription(JsonUtil.stringProperty(node, LinkConstant.PROP_DESCRIPTION));
        link.setServer(ServerReader.readServer(node.get(LinkConstant.PROP_SERVER)));
        ExtensionReader.readExtensions(node, link);
        return link;
    }

    /**
     * Reads an array of LinkParameter annotations into a map.
     *
     * @param annotationValue the annotation value
     * @return map of parameters
     */
    private static Map<String, Object> readLinkParameters(AnnotationScannerContext context, AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        Map<String, Object> linkParams = new LinkedHashMap<>();
        for (AnnotationInstance annotation : nestedArray) {
            String name = context.annotations().value(annotation, LinkConstant.PROP_NAME);
            if (name != null) {
                String expression = context.annotations().value(annotation, LinkConstant.PROP_EXPRESSION);
                linkParams.put(name, expression);
            }
        }
        return linkParams;
    }

    /**
     * Reads the map of {@link Link} parameters.
     *
     * @param node the json node
     * @return map of parameters
     */
    private static Map<String, Object> readLinkParameters(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Object> rval = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            Object value = readObject(node.get(fieldName));
            rval.put(fieldName, value);
        }
        return rval;
    }
}
