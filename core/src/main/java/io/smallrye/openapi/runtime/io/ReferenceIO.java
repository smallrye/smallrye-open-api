package io.smallrye.openapi.runtime.io;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.Reference;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.model.ReferenceType;

public interface ReferenceIO<V, A extends V, O extends V, AB, OB> {

    static final String REF = "$ref";

    JsonIO<V, A, O, AB, OB> jsonIO();

    default boolean isReference(Object model) {
        return model instanceof Reference && isReference((Reference<?>) model);
    }

    default boolean isReference(String name) {
        return REF.equals(name);
    }

    default boolean isReference(Map.Entry<String, ?> entry) {
        return isReference(entry.getKey());
    }

    default boolean isReference(AnnotationInstance annotation) {
        return ReferenceType.isReference(annotation);
    }

    default boolean isReference(Reference<?> model) {
        String ref = model.getRef();
        return ref != null && !ref.trim().isEmpty();
    }

    default String readReference(O node) {
        return jsonIO().getString(node, REF);
    }

    default void setReference(OB object, Reference<?> model) {
        jsonIO().toJson(model.getRef())
                .ifPresent(value -> jsonIO().set(object, REF, value));
    }
}
