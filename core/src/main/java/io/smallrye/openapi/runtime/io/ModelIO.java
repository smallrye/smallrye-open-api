package io.smallrye.openapi.runtime.io;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

public abstract class ModelIO<T, V, A extends V, O extends V, AB, OB> {

    private final IOContext<V, A, O, AB, OB> context;
    protected final DotName annotationName;
    protected final DotName modelName;

    protected ModelIO(IOContext<V, A, O, AB, OB> context, DotName annotationName, DotName modelName) {
        this.context = context;
        this.annotationName = annotationName;
        this.modelName = modelName;
    }

    public JsonIO<V, A, O, AB, OB> jsonIO() {
        return context.jsonIO();
    }

    public AnnotationScannerContext scannerContext() {
        return context.scannerContext();
    }

    protected void setIfPresent(OB object, String key, Optional<? extends V> valueSource) {
        valueSource.ifPresent(value -> jsonIO().set(object, key, value));
    }

    protected void setAllIfPresent(OB object, Optional<? extends O> valueSource) {
        valueSource.ifPresent(value -> jsonIO().setAll(object, value));
    }

    protected Optional<OB> optionalJsonObject(Object source) {
        if (source == null) {
            return Optional.empty();
        }
        return Optional.of(jsonIO().createObject());
    }

    protected Optional<AB> optionalJsonArray(Object source) {
        if (source == null) {
            return Optional.empty();
        }
        return Optional.of(jsonIO().createArray());
    }

    protected static <T> Map.Entry<String, T> entry(String key, T value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    protected <P> P value(AnnotationInstance annotation, String propertyName) {
        return scannerContext().annotations().value(annotation, propertyName);
    }

    protected <P> P value(AnnotationInstance annotation, String propertyName, P defaultValue) {
        return scannerContext().annotations().value(annotation, propertyName, defaultValue);
    }

    protected <P extends Enum<P>> P enumValue(AnnotationInstance annotation, String propertyName, Class<P> type) {
        return scannerContext().annotations().enumValue(annotation, propertyName, type);
    }

    protected <P extends Enum<P>> P enumValue(V value, Class<P> type) {
        String strValue = jsonIO().asString(value);

        if (strValue != null) {
            try {
                return Enum.valueOf(type, strValue.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                // Ignore exceptions from `valueOf` for illegal arguments
            }
        }

        return null;
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
        return scannerContext().annotations().getAnnotation(target, annotationName);
    }

    public List<AnnotationInstance> getRepeatableAnnotations(AnnotationTarget target) {
        return scannerContext().annotations()
                .getRepeatableAnnotation(target, annotationName, Names.containerOf(annotationName));
    }

    public boolean hasRepeatableAnnotation(AnnotationTarget target) {
        return scannerContext().annotations().hasAnnotation(target, annotationName, Names.containerOf(annotationName));
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

    public T readValue(V node) {
        return Optional.ofNullable(node)
                .filter(jsonIO()::isObject)
                .map(jsonIO()::asObject)
                .map(this::readObject)
                .orElse(null);
    }

    public abstract T readObject(O node);

    public abstract Optional<O> write(T model);
}
