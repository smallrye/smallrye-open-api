package io.smallrye.openapi.runtime.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

/**
 * Some utility methods for working with Kotlin types.
 *
 * @author Nicklas Jensen {@literal <nillerr@gmail.com>}
 */
class KotlinUtil {
    /**
     * Constructor.
     */
    private KotlinUtil() {
    }

    private static String getGetterName(FieldInfo field) {
        String fieldName = field.name();
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    private static String getSyntheticPropertyAnnotationsMethodName(FieldInfo field) {
        // Kotlin generates a synthetic method for each property declaring any `AnnotationTarget.PROPERTY` annotations.
        // The name of the property is the getter-name of the field followed by `$annotations`.
        return getGetterName(field) + "$annotations";
    }

    private static Optional<MethodInfo> getSyntheticPropertyAnnotationsMethod(FieldInfo field) {
        String methodName = getSyntheticPropertyAnnotationsMethodName(field);
        ClassInfo clazz = field.declaringClass();

        for (MethodInfo m : clazz.methods()) {
            if (m.isSynthetic() && methodName.equals(m.name())) {
                return Optional.of(m);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the annotations declared on a Kotlin property associated with the field.
     *
     * @param field the field of the associated Kotlin property
     * @return List of annotations declared on the Kotlin property, retargeted at the field
     */
    static List<AnnotationInstance> getPropertyAnnotations(FieldInfo field) {
        return getSyntheticPropertyAnnotationsMethod(field)
                .map(methodInfo -> methodInfo.annotations().stream()
                        .filter(a -> methodInfo.equals(a.target()))
                        .map(a -> AnnotationInstance.create(a.name(), a.runtimeVisible(), field, a.values()))
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }
}
