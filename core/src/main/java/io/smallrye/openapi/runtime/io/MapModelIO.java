package io.smallrye.openapi.runtime.io;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

/**
 *
 * @param <T> model type
 * @param <V> JSON value type
 * @param <A> JSON array type
 * @param <O> JSON object type
 * @param <AB> JSON array builder type (writable array)
 * @param <OB> JSON object builder type (writable object)
 */
public abstract class MapModelIO<T, V, A extends V, O extends V, AB, OB> extends ModelIO<T, V, A, O, AB, OB> {

    protected MapModelIO(IOContext<V, A, O, AB, OB> context, DotName annotationName, DotName modelName) {
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
        if (annotations.isEmpty()) {
            return new LinkedHashMap<>(0);
        }
        IoLogging.logger.annotationsMap('@' + annotationName.local());
        return annotations.stream()
                .map(annotation -> nameFn.apply(annotation).map(name -> entry(name, reader.apply(name, annotation))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toLinkedMap());
    }

    // -------------- JSON

    protected Map<String, T> readMap(O node, Function<O, T> reader) {
        return jsonIO().properties(node)
                .stream()
                .filter(property -> jsonIO().isObject(property.getValue()))
                .map(property -> entry(property.getKey(), reader.apply(jsonIO().asObject(property.getValue()))))
                .collect(toLinkedMap());
    }

    protected Map<String, T> readMap(O node, Predicate<String> nameFilter, Function<V, T> reader) {
        return jsonIO().properties(node)
                .stream()
                .filter(property -> nameFilter.test(property.getKey()))
                .map(property -> entry(property.getKey(), reader.apply(property.getValue())))
                .collect(toLinkedMap());
    }

    public Map<String, T> readMap(V node) {
        return Optional.ofNullable(node)
                .filter(jsonIO()::isObject)
                .map(jsonIO()::asObject)
                .map(this::readObjectMap)
                .orElse(null);
    }

    public Map<String, T> readObjectMap(O node) {
        IoLogging.logger.jsonNodeMap(modelName.local());
        return readMap(node, this::readObject);
    }

    public Optional<O> write(Map<String, T> models) {
        return optionalJsonObject(models).map(node -> {
            models.forEach((key, value) -> {
                Optional<? extends V> jsonValue = write(value);
                if (jsonValue.isPresent()) {
                    jsonIO().set(node, key, jsonValue.get());
                } else {
                    jsonIO().set(node, key, null);
                }
            });
            return node;
        }).map(jsonIO()::buildObject);
    }
}
