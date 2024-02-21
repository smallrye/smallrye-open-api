package io.smallrye.openapi.runtime.io;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public abstract class MapModelIO<T> extends ModelIO<T> {

    protected MapModelIO(AnnotationScannerContext context, DotName annotationName, DotName modelName) {
        super(context, annotationName, modelName);
    }

    // -------------- Annotations

    protected T read(String key, AnnotationInstance annotation) {
        // Default implementation ignores the key
        return read(annotation);
    }

    public Map<String, T> readMap(AnnotationTarget target) {
        return readMap(getRepeatableAnnotations(target));
    }

    public Map<String, T> readMap(AnnotationTarget target, Function<AnnotationInstance, Optional<String>> nameFn) {
        return readMap(getRepeatableAnnotations(target), nameFn);
    }

    public Map<String, T> readMap(AnnotationInstance[] annotations) {
        return readMap(Arrays.asList(annotations));
    }

    public Map<String, T> readMap(AnnotationValue annotations) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(this::readMap)
                .orElse(null);
    }

    public Map<String, T> readMap(Collection<AnnotationInstance> annotations) {
        return readMap(annotations, this::getName, this::read);
    }

    public Map<String, T> readMap(Collection<AnnotationInstance> annotations,
            Function<AnnotationInstance, Optional<String>> nameFn) {
        return readMap(annotations, nameFn, this::read);
    }

    protected Map<String, T> readMap(Collection<AnnotationInstance> annotations,
            Function<AnnotationInstance, Optional<String>> nameFn, BiFunction<String, AnnotationInstance, T> reader) {
        IoLogging.logger.annotationsMap('@' + annotationName.local());
        return annotations.stream()
                .map(annotation -> nameFn.apply(annotation).map(name -> entry(name, reader.apply(name, annotation))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toLinkedMap());
    }

    // -------------- JSON

    protected Map<String, T> readMap(ObjectNode node, Function<ObjectNode, T> reader) {
        return node.properties()
                .stream()
                .filter(property -> Objects.nonNull(property.getValue()))
                .filter(property -> property.getValue().isObject())
                .map(property -> entry(property.getKey(), reader.apply((ObjectNode) property.getValue())))
                .collect(toLinkedMap());
    }

    protected Map<String, T> readMap(ObjectNode node, Predicate<String> nameFilter, Function<JsonNode, T> reader) {
        return node.properties()
                .stream()
                .filter(property -> nameFilter.test(property.getKey()))
                .map(property -> entry(property.getKey(), reader.apply(property.getValue())))
                .collect(toLinkedMap());
    }

    public Map<String, T> readMap(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isObject)
                .map(ObjectNode.class::cast)
                .map(this::readMap)
                .orElse(null);
    }

    public Map<String, T> readMap(ObjectNode node) {
        IoLogging.logger.jsonNodeMap(modelName.local());
        return readMap(node, this::read);
    }

    public Optional<ObjectNode> write(Map<String, T> models) {
        return optionalJsonObject(models).map(node -> {
            models.forEach((key, value) -> {
                Optional<ObjectNode> jsonValue = write(value);
                if (jsonValue.isPresent()) {
                    node.set(key, jsonValue.get());
                } else {
                    node.putNull(key);
                }
            });
            return node;
        });
    }
}
