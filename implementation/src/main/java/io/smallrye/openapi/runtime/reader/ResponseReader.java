package io.smallrye.openapi.runtime.reader;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import io.smallrye.openapi.api.models.responses.APIResponsesImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
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
public class ResponseReader {
    private static final Logger LOG = Logger.getLogger(ResponseReader.class);

    private ResponseReader() {
    }

    /**
     * Reads an array of APIResponse annotations into an {@link APIResponses} model.
     * 
     * @param context the scanning context
     * @param annotationValue {@literal @}APIResponse annotation
     * @return APIResponses model
     */
    public static APIResponses readResponses(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a list of @APIResponse annotations into an APIResponses model.");
        APIResponses responses = new APIResponsesImpl();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String responseCode = JandexUtil.stringValue(nested, MPOpenApiConstants.RESPONSE.PROP_RESPONSE_CODE);
            if (responseCode != null) {
                responses.addAPIResponse(responseCode,
                        ResponseReader.readResponse(context, nested));
            }
        }
        return responses;
    }

    /**
     * Reads a {@link APIResponses} OpenAPI node.
     * 
     * @param node json object
     * @return APIResponses model
     */
    public static APIResponses readResponses(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a json list of APIResponse.");
        APIResponses model = new APIResponsesImpl();
        model.setDefaultValue(readResponse(node.get(MPOpenApiConstants.RESPONSE.PROP_DEFAULT)));
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            if (MPOpenApiConstants.RESPONSE.PROP_DEFAULT.equals(fieldName)) {
                continue;
            }
            model.addAPIResponse(fieldName, readResponse(node.get(fieldName)));
        }
        return model;
    }

    /**
     * Reads a map of APIResponse annotations.
     * 
     * @param context the scanning context
     * @param annotationValue map of {@literal @}APIResponse annotations
     * @return Map of APIResponse models
     */
    public static Map<String, APIResponse> readResponsesMap(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @APIResponse annotations.");
        Map<String, APIResponse> responses = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, MPOpenApiConstants.RESPONSE.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                responses.put(name, readResponse(context, nested));
            }
        }
        return responses;
    }

    /**
     * Reads the {@link APIResponse} OpenAPI nodes.
     * 
     * @param node map of json objects
     * @return Map of APIResponse models
     */
    public static Map<String, APIResponse> readResponsesMap(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a json map of APIResponse.");
        Map<String, APIResponse> responses = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            responses.put(fieldName, readResponse(childNode));
        }

        return responses;
    }

    /**
     * Reads a APIResponse annotation into a model.
     * 
     * @param context the scanning context
     * @param annotationInstance {@literal @}APIResponse annotation
     * @return APIResponse model
     */
    public static APIResponse readResponse(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @Response annotation.");
        APIResponse response = new APIResponseImpl();
        response.setDescription(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.RESPONSE.PROP_DESCRIPTION));
        response.setHeaders(
                HeaderReader.readHeaders(context, annotationInstance.value(MPOpenApiConstants.RESPONSE.PROP_HEADERS)));
        response.setLinks(LinkReader.readLinks(annotationInstance.value(MPOpenApiConstants.RESPONSE.PROP_LINKS)));
        response.setContent(
                ContentReader.readContent(context, annotationInstance.value(MPOpenApiConstants.RESPONSE.PROP_CONTENT),
                        ContentDirection.Output));
        response.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.Response));
        return response;
    }

    /**
     * Reads a {@link APIResponse} OpenAPI node.
     * 
     * @param node the json object
     * @return APIResponse model
     */
    private static APIResponse readResponse(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a single Response json object.");
        APIResponse model = new APIResponseImpl();
        model.setRef(JsonUtil.stringProperty(node, MPOpenApiConstants.RESPONSE.PROP_REF_VAR));
        model.setDescription(JsonUtil.stringProperty(node, MPOpenApiConstants.RESPONSE.PROP_DESCRIPTION));
        model.setHeaders(HeaderReader.readHeaders(node.get(MPOpenApiConstants.RESPONSE.PROP_HEADERS)));
        model.setContent(ContentReader.readContent(node.get(MPOpenApiConstants.RESPONSE.PROP_CONTENT)));
        model.setLinks(LinkReader.readLinks(node.get(MPOpenApiConstants.RESPONSE.PROP_LINKS)));
        ExtensionReader.readExtensions(node, model);
        return model;
    }
}
