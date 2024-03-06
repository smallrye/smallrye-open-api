package io.smallrye.openapi.runtime.io.media;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class MediaTypeIO<V, A extends V, O extends V, AB, OB> extends ModelIO<MediaType, V, A, O, AB, OB> {

    private static final String PROP_EXAMPLE = "example";
    private static final String PROP_EXAMPLES = "examples";
    private static final String PROP_ENCODING = "encoding";
    private static final String PROP_SCHEMA = "schema";

    private final SchemaIO<V, A, O, AB, OB> schemaIO;
    private final ExampleObjectIO<V, A, O, AB, OB> exampleObjectIO;
    private final EncodingIO<V, A, O, AB, OB> encodingIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public MediaTypeIO(IOContext<V, A, O, AB, OB> context, ContentIO<V, A, O, AB, OB> contentIO) {
        super(context, null, Names.create(MediaType.class));
        schemaIO = new SchemaIO<>(context);
        exampleObjectIO = new ExampleObjectIO<>(context);
        encodingIO = new EncodingIO<>(context, contentIO);
        extensionIO = new ExtensionIO<>(context);
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
    public MediaType readObject(O node) {
        IoLogging.logger.singleJsonNode("Content");
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setSchema(schemaIO.readValue(jsonIO().getValue(node, PROP_SCHEMA)));
        mediaType.setExample(jsonIO().fromJson(jsonIO().getValue(node, PROP_EXAMPLE)));
        mediaType.setExamples(exampleObjectIO.readMap(jsonIO().getValue(node, PROP_EXAMPLES)));
        mediaType.setEncoding(encodingIO.readMap(jsonIO().getValue(node, PROP_ENCODING)));
        extensionIO.readMap(node).forEach(mediaType::addExtension);
        return mediaType;
    }

    public Optional<O> write(MediaType model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_SCHEMA, schemaIO.write(model.getSchema()));
            setIfPresent(node, PROP_EXAMPLE, jsonIO().toJson(model.getExample()));
            setIfPresent(node, PROP_EXAMPLES, exampleObjectIO.write(model.getExamples()));
            setIfPresent(node, PROP_ENCODING, encodingIO.write(model.getEncoding()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
