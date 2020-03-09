package io.smallrye.openapi.api.reader;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the APIResponse annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#responseObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ResponseObjectReader {
    private static final Logger LOG = Logger.getLogger(ResponseObjectReader.class);

    private ResponseObjectReader() {
    }

    /**
     * Reads a map of APIResponse annotations.
     * 
     * @param context the scanning context
     * @param annotationValue map of {@literal @}APIResponse annotations
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return Map of APIResponse models
     */
    public static Map<String, APIResponse> readResponses(final AnnotationScannerContext context,
            final AnnotationValue annotationValue,
            final String[] currentConsumes,
            final String[] currentProduces) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @APIResponse annotations.");
        Map<String, APIResponse> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readResponse(context, nested, currentConsumes, currentProduces));
            }
        }
        return map;
    }

    /**
     * Reads a APIResponse annotation into a model.
     * 
     * @param context the scanning context
     * @param annotationInstance {@literal @}APIResponse annotation
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return APIResponse model
     */
    public static APIResponse readResponse(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance,
            final String[] currentConsumes,
            final String[] currentProduces) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @Response annotation.");
        APIResponse response = new APIResponseImpl();
        response.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        response.setHeaders(HeaderReader.readHeaders(context, annotationInstance.value(OpenApiConstants.PROP_HEADERS)));
        response.setLinks(LinkReader.readLinks(annotationInstance.value(OpenApiConstants.PROP_LINKS)));
        response.setContent(MediaTypeObjectReader.readContent(context, annotationInstance.value(OpenApiConstants.PROP_CONTENT),
                ContentDirection.Output, currentConsumes, currentProduces));
        response.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.Response));
        return response;
    }
}
