package io.smallrye.openapi.runtime.io.extensions;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.microprofile.openapi.models.Extensible;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.model.Extensions;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class ExtensionIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Object, V, A, O, AB, OB> {

    private static final String PROP_VALUE = "value";
    private static final String EXTENSION_PROPERTY_PREFIX = "x-";
    private static final String PROP_PARSE_VALUE = "parseValue";

    public static boolean isExtension(String name) {
        return name.toLowerCase().startsWith(EXTENSION_PROPERTY_PREFIX);
    }

    public static boolean isExtension(Map.Entry<String, ?> entry) {
        return isExtension(entry.getKey());
    }

    public ExtensionIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.EXTENSION, Names.create(Object.class));
    }

    @Override
    protected Optional<String> getName(AnnotationInstance annotation) {
        return super.getName(annotation)
                .map(name -> name.startsWith(EXTENSION_PROPERTY_PREFIX) ? name : EXTENSION_PROPERTY_PREFIX.concat(name));
    }

    @Override
    public Object read(AnnotationInstance extension) {
        IoLogging.logger.annotation("@Extension");
        final String extValue = value(extension, PROP_VALUE);
        final Object parsedValue;

        if (Boolean.TRUE.equals(value(extension, PROP_PARSE_VALUE, false))) {
            String name = getName(extension).orElseThrow(IllegalStateException::new);

            parsedValue = scannerContext().getExtensions()
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
    public Map<String, Object> readObjectMap(O node) {
        return readMap(node, ExtensionIO::isExtension, jsonIO()::fromJson);
    }

    @Override
    public Object readObject(O node) {
        return jsonIO().fromJson(node);
    }

    public Optional<O> write(Extensible<?> model) {
        return optionalJsonObject(model.getExtensions()).map(node -> {
            model.getExtensions()
                    .entrySet()
                    .stream()
                    .map(e -> isExtension(e) ? e : entry(EXTENSION_PROPERTY_PREFIX + e.getKey(), e.getValue()))
                    .filter(Predicate.not(e -> Extensions.isPrivateExtension(e.getKey())))
                    .forEach(e -> setIfPresent(node, e.getKey(), jsonIO().toJson(e.getValue())));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
