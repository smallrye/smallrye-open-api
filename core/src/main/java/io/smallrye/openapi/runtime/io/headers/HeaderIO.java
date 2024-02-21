package io.smallrye.openapi.runtime.io.headers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.headers.HeaderImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.Parameterizable;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.media.ExampleObjectIO;
import io.smallrye.openapi.runtime.io.media.SchemaIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class HeaderIO extends MapModelIO<Header> implements ReferenceIO {

    private final SchemaIO schemaIO;
    private final ContentIO contentIO;
    private final ExampleObjectIO exampleObjectIO;
    private final ExtensionIO extensionIO;

    public HeaderIO(AnnotationScannerContext context, ContentIO contentIO) {
        super(context, Names.HEADER, Names.create(Header.class));
        this.contentIO = contentIO;
        exampleObjectIO = new ExampleObjectIO(context);
        schemaIO = new SchemaIO(context);
        extensionIO = new ExtensionIO(context);
    }

    @Override
    public Header read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Header");
        Header header = new HeaderImpl();
        header.setRef(ReferenceType.HEADER.refValue(annotation));
        header.setDescription(context.annotations().value(annotation, Parameterizable.PROP_DESCRIPTION));
        header.setSchema(schemaIO.read(annotation.value(Parameterizable.PROP_SCHEMA)));
        header.setRequired(context.annotations().value(annotation, Parameterizable.PROP_REQUIRED));
        header.setDeprecated(context.annotations().value(annotation, Parameterizable.PROP_DEPRECATED));
        header.setAllowEmptyValue(context.annotations().value(annotation, Parameterizable.PROP_ALLOW_EMPTY_VALUE));
        header.setExtensions(extensionIO.readExtensible(annotation));
        return header;
    }

    @Override
    public Header read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Header");
        Header header = new HeaderImpl();
        header.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        header.setDescription(JsonUtil.stringProperty(node, Parameterizable.PROP_DESCRIPTION));
        header.setSchema(schemaIO.read(node.get(Parameterizable.PROP_SCHEMA)));
        header.setRequired(JsonUtil.booleanProperty(node, Parameterizable.PROP_REQUIRED).orElse(null));
        header.setDeprecated(JsonUtil.booleanProperty(node, Parameterizable.PROP_DEPRECATED).orElse(null));
        header.setAllowEmptyValue(JsonUtil.booleanProperty(node, Parameterizable.PROP_ALLOW_EMPTY_VALUE).orElse(null));
        header.setStyle(readHeaderStyle(node.get(Parameterizable.PROP_STYLE)));
        header.setExplode(JsonUtil.booleanProperty(node, Parameterizable.PROP_EXPLODE).orElse(null));
        header.setExample(JsonUtil.readObject(node.get(Parameterizable.PROP_EXAMPLE)));
        header.setExamples(exampleObjectIO.readMap(node.get(Parameterizable.PROP_EXAMPLES)));
        header.setContent(contentIO.read(node.get(Parameterizable.PROP_CONTENT)));
        extensionIO.readMap(node).forEach(header::addExtension);
        return header;
    }

    @Override
    public Optional<ObjectNode> write(Header model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
            } else {
                JsonUtil.stringProperty(node, Parameterizable.PROP_DESCRIPTION, model.getDescription());
                JsonUtil.booleanProperty(node, Parameterizable.PROP_REQUIRED, model.getRequired());
                JsonUtil.booleanProperty(node, Parameterizable.PROP_DEPRECATED, model.getDeprecated());
                JsonUtil.booleanProperty(node, Parameterizable.PROP_ALLOW_EMPTY_VALUE, model.getAllowEmptyValue());
                JsonUtil.enumProperty(node, Parameterizable.PROP_STYLE, model.getStyle());
                JsonUtil.booleanProperty(node, Parameterizable.PROP_EXPLODE, model.getExplode());
                setIfPresent(node, Parameterizable.PROP_SCHEMA, schemaIO.write(model.getSchema()));
                ObjectWriter.writeObject(node, Parameterizable.PROP_EXAMPLE, model.getExample());
                setIfPresent(node, Parameterizable.PROP_EXAMPLES, exampleObjectIO.write(model.getExamples()));
                setIfPresent(node, Parameterizable.PROP_CONTENT, contentIO.write(model.getContent()));
                setAllIfPresent(node, extensionIO.write(model));
            }
            return node;
        });
    }

    private static Header.Style readHeaderStyle(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return HEADER_STYLE_LOOKUP.get(node.asText());
    }

    private static final Map<String, Header.Style> HEADER_STYLE_LOOKUP = new LinkedHashMap<>();

    static {
        for (Header.Style style : Header.Style.values()) {
            HEADER_STYLE_LOOKUP.put(style.toString(), style);
        }
    }
}
