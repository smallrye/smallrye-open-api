package io.smallrye.openapi.runtime.io.media;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.Encoding.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.media.EncodingImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.headers.HeaderIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class EncodingIO extends MapModelIO<Encoding> {

    private static final String PROP_ALLOW_RESERVED = "allowReserved";
    private static final String PROP_CONTENT_TYPE = "contentType";
    private static final String PROP_HEADERS = "headers";
    private static final String PROP_EXPLODE = "explode";
    private static final String PROP_STYLE = "style";

    private final HeaderIO headerIO;
    private final ExtensionIO extensionIO;

    public EncodingIO(AnnotationScannerContext context, ContentIO contentIO) {
        super(context, Names.ENCODING, Names.create(Encoding.class));
        headerIO = new HeaderIO(context, contentIO);
        extensionIO = new ExtensionIO(context);
    }

    @Override
    public Encoding read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Encoding");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(context.annotations().value(annotation, PROP_CONTENT_TYPE));
        encoding.setStyle(readStyle(annotation));
        encoding.setExplode(context.annotations().value(annotation, PROP_EXPLODE));
        encoding.setAllowReserved(context.annotations().value(annotation, PROP_ALLOW_RESERVED));
        encoding.setHeaders(headerIO.readMap(annotation.value(PROP_HEADERS)));
        encoding.setExtensions(extensionIO.readExtensible(annotation));
        return encoding;
    }

    @Override
    public Encoding read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Encoding");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(JsonUtil.stringProperty(node, PROP_CONTENT_TYPE));
        encoding.setHeaders(headerIO.readMap(node.get(PROP_HEADERS)));
        encoding.setStyle(readStyle(node.get(PROP_STYLE)));
        encoding.setExplode(JsonUtil.booleanProperty(node, PROP_EXPLODE).orElse(null));
        encoding.setAllowReserved(JsonUtil.booleanProperty(node, PROP_ALLOW_RESERVED).orElse(null));
        encoding.setExtensions(extensionIO.readMap(node));
        return encoding;
    }

    public Optional<ObjectNode> write(Encoding model) {
        return optionalJsonObject(model).map(node -> {
            JsonUtil.stringProperty(node, PROP_CONTENT_TYPE, model.getContentType());
            setIfPresent(node, PROP_HEADERS, headerIO.write(model.getHeaders()));
            JsonUtil.enumProperty(node, PROP_STYLE, model.getStyle());
            JsonUtil.booleanProperty(node, PROP_EXPLODE, model.getExplode());
            JsonUtil.booleanProperty(node, PROP_ALLOW_RESERVED, model.getAllowReserved());
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }

    Style readStyle(AnnotationInstance annotation) {
        return Optional.ofNullable(annotation)
                .map(a -> a.value(PROP_STYLE))
                .map(AnnotationValue::asString)
                .flatMap(this::readStyle)
                .orElse(null);
    }

    private Style readStyle(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .flatMap(this::readStyle)
                .orElse(null);
    }

    private Optional<Style> readStyle(String style) {
        return Arrays.stream(Style.values())
                .filter(value -> style.equals(value.toString()))
                .findFirst();
    }
}
