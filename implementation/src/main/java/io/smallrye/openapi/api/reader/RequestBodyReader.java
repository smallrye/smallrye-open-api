package io.smallrye.openapi.api.reader;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.parameters.RequestBodyImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the RequestBody annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#requestBodyObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class RequestBodyReader {
    private static final Logger LOG = Logger.getLogger(RequestBodyReader.class);

    private RequestBodyReader() {
    }

    /**
     * Reads a map of RequestBody annotations.
     * 
     * @param context the scanning context
     * @param annotationValue map of {@literal @}RequestBody annotations
     * @return Map of RequestBody model
     */
    public static Map<String, RequestBody> readRequestBodies(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @RequestBody annotations.");
        Map<String, RequestBody> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readRequestBody(context, nested));
            }
        }
        return map;
    }

    /**
     * Reads a RequestBody annotation into a model.
     * 
     * @param context the scanning context
     * @param annotationValue {@literal @}RequestBody annotation
     * @return RequestBody model
     */
    public static RequestBody readRequestBody(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        return readRequestBody(context, annotationValue.asNested());
    }

    /**
     * Reads a RequestBody annotation into a model.
     * 
     * @param context the scanning context
     * @param annotationInstance {@literal @}RequestBody annotation
     * @return RequestBody model
     */
    public static RequestBody readRequestBody(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @RequestBody annotation.");
        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        requestBody
                .setContent(MediaTypeObjectReader.readContent(context, annotationInstance.value(OpenApiConstants.PROP_CONTENT),
                        ContentDirection.Input));
        requestBody.setRequired(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_REQUIRED));
        requestBody.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.RequestBody));
        return requestBody;
    }

}
