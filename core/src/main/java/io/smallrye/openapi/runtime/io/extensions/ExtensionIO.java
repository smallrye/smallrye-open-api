package io.smallrye.openapi.runtime.io.extensions;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.Extensible;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class ExtensionIO extends MapModelIO<Object> {

    private static final String PROP_VALUE = "value";
    private static final String EXTENSION_PROPERTY_PREFIX = "x-";
    private static final String PROP_PARSE_VALUE = "parseValue";

    public static boolean isExtension(String name) {
        return name.toLowerCase().startsWith(EXTENSION_PROPERTY_PREFIX);
    }

    public static boolean isExtension(Map.Entry<String, ?> entry) {
        return isExtension(entry.getKey());
    }

    public ExtensionIO(AnnotationScannerContext context) {
        super(context, Names.EXTENSION, Names.create(Object.class));
    }

    @Override
    public Object read(AnnotationInstance extension) {
        IoLogging.logger.annotation("@Extension");
        final String extValue = value(extension, PROP_VALUE);
        final Object parsedValue;

        if (Boolean.TRUE.equals(value(extension, PROP_PARSE_VALUE, false))) {
            String name = getName(extension).orElseThrow(IllegalStateException::new);

            parsedValue = context.getExtensions()
                    .stream()
                    .map(ext -> ext.parseExtension(name, extValue))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(extValue);
        } else {
            parsedValue = extValue;
        }

        return parsedValue;
    }

    public Map<String, Object> readExtensible(AnnotationInstance extensible) {
        AnnotationInstance[] extensions = Optional.ofNullable(extensible.value("extensions"))
                .map(AnnotationValue::asNestedArray)
                .orElseGet(() -> getRepeatableAnnotations(extensible.target())
                        .stream()
                        .toArray(AnnotationInstance[]::new));

        if (extensions.length > 0) {
            return readMap(extensions);
        }

        return null; // NOSONAR - an empty map is not wanted
    }

    @Override
    public Map<String, Object> readMap(ObjectNode node) {
        return readMap(node, ExtensionIO::isExtension, JsonUtil::readObject);
    }

    @Override
    public Object read(ObjectNode node) {
        return JsonUtil.readObject(node);
    }

    @Override
    public Optional<ObjectNode> write(Object model) {
        return Optional.ofNullable(model)
                .filter(Extensible.class::isInstance)
                .map(Extensible.class::cast)
                .flatMap(this::write);
    }

    public Optional<ObjectNode> write(Extensible<?> model) {
        return optionalJsonObject(model.getExtensions())
                .map(node -> {
                    model.getExtensions()
                            .entrySet()
                            .stream()
                            .map(e -> isExtension(e) ? e
                                    : entry(EXTENSION_PROPERTY_PREFIX + e.getKey(), e.getValue()))
                            .forEach(e -> ObjectWriter.writeObject(node, e.getKey(), e.getValue()));
                    return node;
                });
    }
}
