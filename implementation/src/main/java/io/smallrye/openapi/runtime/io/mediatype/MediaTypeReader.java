package io.smallrye.openapi.runtime.io.mediatype;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.runtime.io.encoding.EncodingReader;
import io.smallrye.openapi.runtime.io.example.ExampleReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.io.schema.SchemaReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Media type object annotation and json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#mediaTypeObject">mediaTypeObject</a>
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
        mediaType.setExamples(ExampleReader.readExamples(annotationInstance.value(MediaTypeConstant.PROP_EXAMPLES)));
        mediaType.setExample(JandexUtil.stringValue(annotationInstance, MediaTypeConstant.PROP_EXAMPLE));
        mediaType.setSchema(SchemaFactory.readSchema(context.getIndex(),
                annotationInstance.value(MediaTypeConstant.PROP_SCHEMA)));
        mediaType.setEncoding(
                EncodingReader.readEncodings(context, annotationInstance.value(MediaTypeConstant.PROP_ENCODING)));
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
        mediaType.setSchema(SchemaReader.readSchema(node.get(MediaTypeConstant.PROP_SCHEMA)));
        mediaType.setExample(readObject(node.get(MediaTypeConstant.PROP_EXAMPLE)));
        mediaType.setExamples(ExampleReader.readExamples(node.get(MediaTypeConstant.PROP_EXAMPLES)));
        mediaType.setEncoding(EncodingReader.readEncodings(node.get(MediaTypeConstant.PROP_ENCODING)));
        ExtensionReader.readExtensions(node, mediaType);
        return mediaType;
    }

}
