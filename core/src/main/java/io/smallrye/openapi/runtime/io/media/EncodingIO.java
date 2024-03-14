package io.smallrye.openapi.runtime.io.media;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.Encoding.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.api.models.media.EncodingImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.headers.HeaderIO;

public class EncodingIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Encoding, V, A, O, AB, OB> {

    private static final String PROP_ALLOW_RESERVED = "allowReserved";
    private static final String PROP_CONTENT_TYPE = "contentType";
    private static final String PROP_HEADERS = "headers";
    private static final String PROP_EXPLODE = "explode";
    private static final String PROP_STYLE = "style";

    private final HeaderIO<V, A, O, AB, OB> headerIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public EncodingIO(IOContext<V, A, O, AB, OB> context, ContentIO<V, A, O, AB, OB> contentIO,
            ExtensionIO<V, A, O, AB, OB> extensionIO) {
        super(context, Names.ENCODING, Names.create(Encoding.class));
        headerIO = new HeaderIO<>(context, contentIO, extensionIO);
        this.extensionIO = extensionIO;
    }

    @Override
    public Encoding read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Encoding");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(value(annotation, PROP_CONTENT_TYPE));
        encoding.setStyle(readStyle(annotation));
        encoding.setExplode(value(annotation, PROP_EXPLODE));
        encoding.setAllowReserved(value(annotation, PROP_ALLOW_RESERVED));
        encoding.setHeaders(headerIO.readMap(annotation.value(PROP_HEADERS)));
        encoding.setExtensions(extensionIO.readExtensible(annotation));
        return encoding;
    }

    @Override
    public Encoding readObject(O node) {
        IoLogging.logger.singleJsonNode("Encoding");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(jsonIO().getString(node, PROP_CONTENT_TYPE));
        encoding.setHeaders(headerIO.readMap(jsonIO().getValue(node, PROP_HEADERS)));
        encoding.setStyle(readStyle(jsonIO().getValue(node, PROP_STYLE)));
        encoding.setExplode(jsonIO().getBoolean(node, PROP_EXPLODE));
        encoding.setAllowReserved(jsonIO().getBoolean(node, PROP_ALLOW_RESERVED));
        encoding.setExtensions(extensionIO.readMap(node));
        return encoding;
    }

    @Override
    public Optional<O> write(Encoding model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_CONTENT_TYPE, jsonIO().toJson(model.getContentType()));
            setIfPresent(node, PROP_HEADERS, headerIO.write(model.getHeaders()));
            setIfPresent(node, PROP_STYLE, jsonIO().toJson(model.getStyle()));
            setIfPresent(node, PROP_EXPLODE, jsonIO().toJson(model.getExplode()));
            setIfPresent(node, PROP_ALLOW_RESERVED, jsonIO().toJson(model.getAllowReserved()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }

    Style readStyle(AnnotationInstance annotation) {
        return Optional.ofNullable(annotation)
                .map(a -> a.value(PROP_STYLE))
                .map(AnnotationValue::asString)
                .flatMap(this::readStyle)
                .orElse(null);
    }

    private Style readStyle(V node) {
        return Optional.ofNullable(node)
                .map(jsonIO()::asString)
                .flatMap(this::readStyle)
                .orElse(null);
    }

    private Optional<Style> readStyle(String style) {
        return Arrays.stream(Style.values())
                .filter(value -> style.equals(value.toString()))
                .findFirst();
    }
}
