package io.smallrye.openapi.runtime.io;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.Reference;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.util.JandexUtil;

public interface ReferenceIO {

    default boolean isReference(Object model) {
        return model instanceof Reference && isReference((Reference<?>) model);
    }

    default boolean isReference(String name) {
        return Referenceable.PROP_$REF.equals(name);
    }

    default boolean isReference(Map.Entry<String, ?> entry) {
        return isReference(entry.getKey());
    }

    default boolean isReference(AnnotationInstance annotation) {
        return JandexUtil.isRef(annotation);
    }

    default boolean isReference(Reference<?> model) {
        String ref = model.getRef();
        return ref != null && !ref.trim().isEmpty();
    }

    default String readReference(ObjectNode node) {
        return JsonUtil.stringProperty(node, Referenceable.PROP_$REF);
    }
}
