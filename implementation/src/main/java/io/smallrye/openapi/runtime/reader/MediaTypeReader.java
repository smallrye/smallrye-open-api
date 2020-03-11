package io.smallrye.openapi.runtime.reader;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.SchemaFactory;

/**
 * Reading the Media type object annotation and json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#mediaTypeObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class MediaTypeReader {
    private static final Logger LOG = Logger.getLogger(MediaTypeReader.class);

    private MediaTypeReader() {
    }

    /**
     * Reads a single Content annotation into a {@link MediaType} model.
     * 
     * @param context the scanning context
     * @param annotationInstance {@literal @}Content annotation
     * @return MediaType model
     */
    public static MediaType readMediaType(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @Content annotation as a MediaType.");
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setExamples(ExampleReader.readExamples(annotationInstance.value(MPOpenApiConstants.MEDIATYPE.PROP_EXAMPLES)));
        mediaType.setExample(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.MEDIATYPE.PROP_EXAMPLE));
        mediaType.setSchema(SchemaFactory.readSchema(context.getIndex(),
                annotationInstance.value(MPOpenApiConstants.MEDIATYPE.PROP_SCHEMA)));
        mediaType.setEncoding(
                EncodingReader.readEncodings(context, annotationInstance.value(MPOpenApiConstants.MEDIATYPE.PROP_ENCODING)));
        return mediaType;
    }

    /**
     * Reads a {@link MediaType} OpenAPI node.
     * 
     * @param node
     * @return
     */
    public static MediaType readMediaType(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a single Content json node.");
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setSchema(SchemaReader.readSchema(node.get(MPOpenApiConstants.MEDIATYPE.PROP_SCHEMA)));
        mediaType.setExample(readObject(node.get(MPOpenApiConstants.MEDIATYPE.PROP_EXAMPLE)));
        mediaType.setExamples(ExampleReader.readExamples(node.get(MPOpenApiConstants.MEDIATYPE.PROP_EXAMPLES)));
        mediaType.setEncoding(EncodingReader.readEncodings(node.get(MPOpenApiConstants.MEDIATYPE.PROP_ENCODING)));
        ExtensionReader.readExtensions(node, mediaType);
        return mediaType;
    }

}
