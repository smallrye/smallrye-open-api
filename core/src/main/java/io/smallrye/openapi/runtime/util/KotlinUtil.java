package io.smallrye.openapi.runtime.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
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
        return "get" + field.name().substring(0, 1).toUpperCase() + field.name().substring(1);
    }

    private static String getSyntheticPropertyAnnotationsMethodName(FieldInfo field) {
        // Kotlin generates a synthetic method for each property declaring any `AnnotationTarget.PROPERTY` annotations.
        // The name of the property is the getter-name of the field followed by `$annotations`.
        return getGetterName(field) + "$annotations";
    }

    private static Optional<MethodInfo> getSyntheticPropertyAnnotationsMethod(FieldInfo field) {
        var methodName = getSyntheticPropertyAnnotationsMethodName(field);
        var clazz = field.declaringClass();
        return clazz.methods().stream()
                .filter(m -> m.isSynthetic() && methodName.equals(m.name()))
                .findFirst();
    }

    /**
     * Returns the annotations declared on a Kotlin property associated with the field.
     * @param field the field of the associated Kotlin property
     * @return List of annotations declared on the Kotlin property, retargeted at the field
     */
    static List<AnnotationInstance> getPropertyAnnotations(FieldInfo field) {
        return getSyntheticPropertyAnnotationsMethod(field).stream()
                .flatMap(methodInfo -> methodInfo.annotations().stream()
                        .filter(a -> methodInfo.equals(a.target()))
                        .map(a -> AnnotationInstance.create(a.name(), a.runtimeVisible(), field, a.values())))
                .collect(Collectors.toList());
    }
}
