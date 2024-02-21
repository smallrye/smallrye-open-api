package io.smallrye.openapi.runtime.io.media;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class MediaTypeIO extends ModelIO<MediaType> {

    private static final String PROP_EXAMPLE = "example";
    private static final String PROP_EXAMPLES = "examples";
    private static final String PROP_ENCODING = "encoding";
    private static final String PROP_SCHEMA = "schema";

    private final SchemaIO schemaIO;
    private final ExampleObjectIO exampleObjectIO;
    private final EncodingIO encodingIO;
    private final ExtensionIO extensionIO;

    public MediaTypeIO(AnnotationScannerContext context, ContentIO contentIO) {
        super(context, null, Names.create(MediaType.class));
        schemaIO = new SchemaIO(context);
        exampleObjectIO = new ExampleObjectIO(context);
        encodingIO = new EncodingIO(context, contentIO);
        extensionIO = new ExtensionIO(context);
    }

    @Override
    public MediaType read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotationAs("@Content", "MediaType");
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setExamples(exampleObjectIO.readMap(annotation.value(PROP_EXAMPLES)));
        mediaType.setExample(exampleObjectIO.parseValue(value(annotation, PROP_EXAMPLE)));
        mediaType.setSchema(schemaIO.read(annotation.value(PROP_SCHEMA)));
        mediaType.setEncoding(encodingIO.readMap(annotation.value(PROP_ENCODING)));
        mediaType.setExtensions(extensionIO.readExtensible(annotation));
        return mediaType;
    }

    @Override
    public MediaType read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Content");
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setSchema(schemaIO.read(node.get(PROP_SCHEMA)));
        mediaType.setExample(JsonUtil.readObject(node.get(PROP_EXAMPLE)));
        mediaType.setExamples(exampleObjectIO.readMap(node.get(PROP_EXAMPLES)));
        mediaType.setEncoding(encodingIO.readMap(node.get(PROP_ENCODING)));
        extensionIO.readMap(node).forEach(mediaType::addExtension);
        return mediaType;
    }

    public Optional<ObjectNode> write(MediaType model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_SCHEMA, schemaIO.write(model.getSchema()));
            ObjectWriter.writeObject(node, PROP_EXAMPLE, model.getExample());
            setIfPresent(node, PROP_EXAMPLES, exampleObjectIO.write(model.getExamples()));
            setIfPresent(node, PROP_ENCODING, encodingIO.write(model.getEncoding()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }
}
