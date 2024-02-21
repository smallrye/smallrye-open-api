package io.smallrye.openapi.runtime.io;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

public abstract class ModelIO<T> {

    protected final AnnotationScannerContext context;
    protected final DotName annotationName;
    protected final DotName modelName;

    protected ModelIO(AnnotationScannerContext context, DotName annotationName, DotName modelName) {
        this.context = context;
        this.annotationName = annotationName;
        this.modelName = modelName;
    }

    protected ObjectNode createObject() {
        return JsonUtil.objectNode();
    }

    protected void setIfPresent(ObjectNode object, String key, Optional<? extends JsonNode> valueSource) {
        valueSource.ifPresent(node -> object.set(key, node));
    }

    protected void setAllIfPresent(ObjectNode object, Optional<? extends ObjectNode> valueSource) {
        valueSource.ifPresent(object::setAll);
    }

    protected Optional<ObjectNode> optionalJsonObject(Object source) {
        if (source == null) {
            return Optional.empty();
        }
        return Optional.of(JsonUtil.objectNode());
    }

    protected Optional<ArrayNode> optionalJsonArray(Object source) {
        if (source == null) {
            return Optional.empty();
        }
        return Optional.of(JsonUtil.arrayNode());
    }

    protected static <T> Map.Entry<String, T> entry(String key, T value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    protected <V> V value(AnnotationInstance annotation, String propertyName) {
        return context.annotations().value(annotation, propertyName);
    }

    protected <V extends Enum<V>> V enumValue(AnnotationInstance annotation, String propertyName, Class<V> type) {
        return context.annotations().enumValue(annotation, propertyName, type);
    }

    protected <V> V value(AnnotationInstance annotation, String propertyName, V defaultValue) {
        return context.annotations().value(annotation, propertyName, defaultValue);
    }

    protected Optional<String> getName(AnnotationInstance annotation, String nameProperty) {
        String name = value(annotation, nameProperty);

        if (name == null && JandexUtil.isRef(annotation)) {
            name = JandexUtil.nameFromRef(annotation);
        }

        return Optional.ofNullable(name);
    }

    protected Optional<String> getName(AnnotationInstance annotation) {
        return getName(annotation, "name");
    }

    @SuppressWarnings("unchecked")
    protected static <T> Predicate<T> not(Predicate<? super T> predicate) {
        return (Predicate<T>) predicate.negate();
    }

    protected static <T> Collector<Map.Entry<String, T>, ?, Map<String, T>> toLinkedMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new);
    }

    public AnnotationInstance getAnnotation(AnnotationTarget target) {
        return context.annotations().getAnnotation(target, annotationName);
    }

    public List<AnnotationInstance> getRepeatableAnnotations(AnnotationTarget target) {
        return context.annotations()
                .getRepeatableAnnotation(target, annotationName, Names.containerOf(annotationName));
    }

    public boolean hasRepeatableAnnotation(AnnotationTarget target) {
        return context.annotations().hasAnnotation(target, annotationName, Names.containerOf(annotationName));
    }

    public T read(AnnotationTarget target) {
        return Optional.ofNullable(getAnnotation(target))
                .map(this::read)
                .orElse(null);
    }

    public T read(AnnotationValue annotation) {
        return Optional.ofNullable(annotation)
                .map(AnnotationValue::asNested)
                .map(this::read)
                .orElse(null);
    }

    public abstract T read(AnnotationInstance annotation);

    public T read(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isObject)
                .map(ObjectNode.class::cast)
                .map(this::read)
                .orElse(null);
    }

    public abstract T read(ObjectNode node);

    public abstract Optional<ObjectNode> write(T model);
}
