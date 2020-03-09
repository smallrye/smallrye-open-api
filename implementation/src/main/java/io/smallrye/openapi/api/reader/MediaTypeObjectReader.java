package io.smallrye.openapi.api.reader;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.Encoding.Style;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.EncodingImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.SchemaFactory;

/**
 * Reading the Media type object annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#mediaTypeObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class MediaTypeObjectReader {
    private static final Logger LOG = Logger.getLogger(MediaTypeObjectReader.class);

    private MediaTypeObjectReader() {
    }

    /**
     * Reads a single Content annotation into a model. The value in this case is an array of
     * Content annotations.
     * 
     * @param context the scanning context
     * @param annotationValue the {@literal @}Content annotation
     * @param direction the content direction
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return Content model
     */
    public static Content readContent(final AnnotationScannerContext context,
            final AnnotationValue annotationValue,
            final ContentDirection direction,
            final String[] currentConsumes,
            final String[] currentProduces) {

        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a single @Content annotation.");
        Content content = new ContentImpl();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String contentType = JandexUtil.stringValue(nested, OpenApiConstants.PROP_MEDIA_TYPE);
            MediaType mediaTypeModel = readMediaType(context, nested);
            if (contentType == null) {
                // If the content type is not provided in the @Content annotation, then
                // we assume it applies to all the jax-rs method's @Consumes or @Produces
                String[] mimeTypes = {};
                if (direction == ContentDirection.Input && currentConsumes != null) {
                    mimeTypes = currentConsumes;
                }
                if (direction == ContentDirection.Output && currentProduces != null) {
                    mimeTypes = currentProduces;
                }
                if (direction == ContentDirection.Parameter) {
                    mimeTypes = OpenApiConstants.DEFAULT_MEDIA_TYPES.get();
                }
                for (String mimeType : mimeTypes) {
                    content.addMediaType(mimeType, mediaTypeModel);
                }
            } else {
                content.addMediaType(contentType, mediaTypeModel);
            }
        }
        return content;
    }

    /**
     * Reads a single Content annotation into a {@link MediaType} model.
     * 
     * @param context the scanning context
     * @param annotationInstance {@literal @}Content annotation
     * @return MediaType model
     */
    private static MediaType readMediaType(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @Content annotation as a MediaType.");
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setExamples(ExampleReader.readExamples(annotationInstance.value(OpenApiConstants.PROP_EXAMPLES)));
        mediaType.setExample(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_EXAMPLE));
        mediaType.setSchema(
                SchemaFactory.readSchema(context.getIndex(), annotationInstance.value(OpenApiConstants.PROP_SCHEMA)));
        mediaType.setEncoding(readEncodings(context, annotationInstance.value(OpenApiConstants.PROP_ENCODING)));
        return mediaType;
    }

    /**
     * Reads an array of Encoding annotations as a Map.
     * 
     * @param context the scanning context
     * @param annotationValue Map of {@literal @}Encoding annotations
     * @return Map of Encoding models
     */
    private static Map<String, Encoding> readEncodings(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @Encoding annotations.");
        Map<String, Encoding> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readEncoding(context, annotation));
            }
        }
        return map;
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
        LOG.debug("Processing a single @Encoding annotation.");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_CONTENT_TYPE));
        encoding.setStyle(JandexUtil.enumValue(annotationInstance, OpenApiConstants.PROP_STYLE, Style.class));
        encoding.setExplode(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_EXPLODE));
        encoding.setAllowReserved(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_ALLOW_RESERVED));
        encoding.setHeaders(HeaderReader.readHeaders(context, annotationInstance.value(OpenApiConstants.PROP_HEADERS)));
        return encoding;
    }
}
