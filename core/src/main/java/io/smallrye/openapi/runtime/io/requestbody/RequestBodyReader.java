package io.smallrye.openapi.runtime.io.requestbody;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.parameters.RequestBodyImpl;
import io.smallrye.openapi.runtime.io.ContentDirection;
import io.smallrye.openapi.runtime.io.CurrentScannerInfo;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.content.ContentReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Reading the RequestBody annotation
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#requestBodyObject">requestBodyObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class RequestBodyReader {

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
        IoLogging.logger.annotationsMap("@RequestBody");
        Map<String, RequestBody> requestBodies = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, RequestBodyConstant.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                requestBodies.put(name, readRequestBody(context, nested));
            }
        }
        return requestBodies;
    }

    /**
     * Reads the {@link RequestBody} OpenAPI nodes.
     * 
     * @param node json map of Request Bodies
     * @return Map of RequestBody model
     */
    public static Map<String, RequestBody> readRequestBodies(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.logger.jsonMap("RequestBody");
        Map<String, RequestBody> requestBodies = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            requestBodies.put(fieldName, readRequestBody(childNode));
        }
        return requestBodies;
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
        IoLogging.logger.singleAnnotation("@RequestBody");
        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setDescription(JandexUtil.stringValue(annotationInstance, RequestBodyConstant.PROP_DESCRIPTION));
        requestBody
                .setContent(ContentReader.readContent(context,
                        annotationInstance.value(RequestBodyConstant.PROP_CONTENT),
                        ContentDirection.INPUT));
        requestBody.setRequired(JandexUtil.booleanValue(annotationInstance, RequestBodyConstant.PROP_REQUIRED).orElse(null));
        requestBody.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.REQUEST_BODY));
        requestBody.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
        return requestBody;
    }

    /**
     * Reads a RequestBodySchema annotation into a model.
     * 
     * @param context the scanning context
     * @param annotation {@literal @}RequestBodySchema annotation
     * @return RequestBody model
     */
    public static RequestBody readRequestBodySchema(final AnnotationScannerContext context,
            AnnotationInstance annotation) {
        if (annotation == null || CurrentScannerInfo.getCurrentConsumes() == null) {
            // Only generate the RequestBody if the endpoint declares an @Consumes media type
            return null;
        }
        IoLogging.logger.singleAnnotation("@RequestBodySchema");
        Content content = new ContentImpl();

        for (String mediaType : CurrentScannerInfo.getCurrentConsumes()) {
            MediaType type = new MediaTypeImpl();
            type.setSchema(SchemaFactory.typeToSchema(context,
                    JandexUtil.value(annotation, RequestBodyConstant.PROP_VALUE),
                    context.getExtensions()));
            content.addMediaType(mediaType, type);
        }

        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setContent(content);

        return requestBody;
    }

    /**
     * Reads a {@link RequestBody} OpenAPI node.
     * 
     * @param node the json object
     * @return RequestBody model
     */
    public static RequestBody readRequestBody(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setDescription(JsonUtil.stringProperty(node, RequestBodyConstant.PROP_DESCRIPTION));
        requestBody.setContent(ContentReader.readContent(node.get(RequestBodyConstant.PROP_CONTENT)));
        requestBody.setRequired(JsonUtil.booleanProperty(node, RequestBodyConstant.PROP_REQUIRED).orElse(null));
        requestBody.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        ExtensionReader.readExtensions(node, requestBody);
        return requestBody;
    }

    // helper methods for scanners
    public static List<AnnotationInstance> getRequestBodyAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                RequestBodyConstant.DOTNAME_REQUESTBODY, null);
    }

    public static AnnotationInstance getRequestBodySchemaAnnotation(final AnnotationTarget target) {
        return TypeUtil.getAnnotation(target, RequestBodyConstant.DOTNAME_REQUEST_BODY_SCHEMA);
    }
}
