package io.smallrye.openapi.runtime.io.content;

import java.util.Iterator;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.runtime.io.ContentDirection;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.mediatype.MediaTypeReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.Annotations;

/**
 * Reading the Content object annotation and json
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ContentReader {

    private ContentReader() {
    }

    /**
     * Reads a single Content annotation into a model. The value in this case is an array of
     * Content annotations.
     *
     * @param context the scanning context
     * @param annotationValue the {@literal @}Content annotation
     * @param direction the content direction
     * @return Content model
     */
    public static Content readContent(final AnnotationScannerContext context,
            final AnnotationValue annotationValue,
            final ContentDirection direction) {

        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.singleAnnotation("@Content");
        Content content = new ContentImpl();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String contentType = Annotations.value(nested, OpenApiConstants.PROP_MEDIA_TYPE);
            MediaType mediaTypeModel = MediaTypeReader.readMediaType(context, nested);
            if (contentType == null) {
                for (String mimeType : getDefaultMimeTypes(context, direction)) {
                    content.addMediaType(mimeType, mediaTypeModel);
                }
            } else {
                content.addMediaType(contentType, mediaTypeModel);
            }
        }
        return content;
    }

    /**
     * Reads a {@link Content} OpenAPI node.
     *
     * @param node the json node
     * @return Content model
     */
    public static Content readContent(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Content content = new ContentImpl();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            content.addMediaType(fieldName, MediaTypeReader.readMediaType(node.get(fieldName)));
        }
        return content;
    }

    /**
     * If the content type is not provided in the @Content annotation, then
     * we assume it applies to all the scanner method's @Consumes or @Produces
     *
     * @param direction the flow of traffic
     * @return default mimetypes
     */
    private static String[] getDefaultMimeTypes(AnnotationScannerContext context, final ContentDirection direction) {

        if (direction == ContentDirection.INPUT && context.getCurrentConsumes() != null) {
            return context.getCurrentConsumes();
        } else if (direction == ContentDirection.OUTPUT && context.getCurrentProduces() != null) {
            return context.getCurrentProduces();
        } else if (direction == ContentDirection.PARAMETER) {
            return OpenApiConstants.DEFAULT_MEDIA_TYPES.get();
        } else {
            return new String[] {};
        }
    }

}
