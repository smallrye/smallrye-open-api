package io.smallrye.openapi.api.reader;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.links.Link;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.links.LinkImpl;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Link annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#linkObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class LinkReader {
    private static final Logger LOG = Logger.getLogger(LinkReader.class);

    private LinkReader() {
    }

    /**
     * Reads Link annotations
     * 
     * @param annotationValue map of {@literal @}Link annotations
     * @return Map of Link model
     */
    public static Map<String, Link> readLinks(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @Link annotations.");
        Map<String, Link> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readLink(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Link annotation into a model.
     * 
     * @param annotationInstance {@literal @}Link annotation
     * @return Link model
     */
    private static Link readLink(final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @Link annotation.");
        Link link = new LinkImpl();
        link.setOperationRef(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_OPERATION_REF));
        link.setOperationId(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_OPERATION_ID));
        link.setParameters(readLinkParameters(annotationInstance.value(OpenApiConstants.PROP_PARAMETERS)));
        link.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        link.setRequestBody(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_REQUEST_BODY));
        link.setServer(ServerReader.readServer(annotationInstance.value(OpenApiConstants.PROP_SERVER)));
        link.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.Link));
        return link;
    }

    /**
     * Reads an array of LinkParameter annotations into a map.
     * 
     * @param annotationValue
     */
    private static Map<String, Object> readLinkParameters(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        Map<String, Object> linkParams = new LinkedHashMap<>();
        for (AnnotationInstance annotation : nestedArray) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            if (name != null) {
                String expression = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXPRESSION);
                linkParams.put(name, expression);
            }
        }
        return linkParams;
    }
}
