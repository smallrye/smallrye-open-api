package io.smallrye.openapi.runtime.scanner.dataobject;

import java.lang.reflect.Modifier;
import java.util.*;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.constants.JacksonConstants;
import io.smallrye.openapi.api.constants.JsonbConstants;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.dataobject.DataObjectDeque.PathEntry;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class IgnoreResolver {

    private final Map<DotName, IgnoreAnnotationHandler> IGNORE_ANNOTATION_MAP = new LinkedHashMap<>();
    private final AugmentedIndexView index;

    {
        IgnoreAnnotationHandler[] ignoreHandlers = {
                new SchemaHiddenHandler(),
                new JsonbTransientHandler(),
                new JsonIgnorePropertiesHandler(),
                new JsonIgnoreHandler(),
                new JsonIgnoreTypeHandler(),
                new TransientIgnoreHandler()
        };

        for (IgnoreAnnotationHandler handler : ignoreHandlers) {
            IGNORE_ANNOTATION_MAP.put(handler.getName(), handler);
        }
    }

    public IgnoreResolver(AugmentedIndexView index) {
        this.index = index;
    }

    public boolean isIgnore(AnnotationTarget annotationTarget, DataObjectDeque.PathEntry pathEntry) {
        for (IgnoreAnnotationHandler handler : IGNORE_ANNOTATION_MAP.values()) {
            boolean result = handler.shouldIgnore(annotationTarget, pathEntry);
            if (result) {
                return true;
            }
        }
        return false;
    }

    public ClassInfo getClassInfoFromIndex(Type type) {
        return this.index.getClass(type);
    }

    /**
     * Handler for OAS hidden @{@link Schema}
     */
    private final class SchemaHiddenHandler implements IgnoreAnnotationHandler {
        @Override
        public boolean shouldIgnore(AnnotationTarget target, PathEntry parentPathEntry) {
            AnnotationInstance annotationInstance = TypeUtil.getAnnotation(target, getName());
            if (annotationInstance != null) {
                return JandexUtil.booleanValue(annotationInstance, SchemaConstant.PROP_HIDDEN).orElse(false);
            }
            return false;
        }

        @Override
        public DotName getName() {
            return SchemaConstant.DOTNAME_SCHEMA;
        }
    }

    /**
     * Handler for JSON-B's @{@link javax.json.bind.annotation.JsonbTransient}
     */
    private final class JsonbTransientHandler implements IgnoreAnnotationHandler {
        @Override
        public boolean shouldIgnore(AnnotationTarget target, DataObjectDeque.PathEntry parentPathEntry) {
            return TypeUtil.hasAnnotation(target, getName());
        }

        @Override
        public DotName getName() {
            return JsonbConstants.JSONB_TRANSIENT;
        }
    }

    /**
     * Handler for Jackson's {@link com.fasterxml.jackson.annotation.JsonIgnoreProperties JsonIgnoreProperties}
     */
    private final class JsonIgnorePropertiesHandler implements IgnoreAnnotationHandler {

        @Override
        public boolean shouldIgnore(AnnotationTarget target, DataObjectDeque.PathEntry parentPathEntry) {
            if (declaringClassIgnore(target)) {
                return true;
            }
            if (superClassIgnore(target)) {
                return true;
            }

            return nestingPropertyIgnore(parentPathEntry.getAnnotationTarget(), propertyName(target));
        }

        /**
         * Declaring class ignore
         *
         * <pre>
         * <code>
         *  &#64;JsonIgnoreProperties("ignoreMe")
         *  class A {
         *    String ignoreMe;
         *  }
         * </code>
         * </pre>
         *
         * @param target
         * @return
         */
        private boolean declaringClassIgnore(AnnotationTarget target) {
            AnnotationInstance declaringClassJIP = TypeUtil.getAnnotation(TypeUtil.getDeclaringClass(target), getName());
            return shouldIgnoreTarget(declaringClassJIP, propertyName(target));
        }

        /**
         * Super class ignore
         *
         * <pre>
         * <code>
         *  &#64;JsonIgnoreProperties("ignoreMe")
         *  class A {
         *    String ignoreMe;
         *    getIgnoreMe() {
         *        ...
         *    }
         *  }
         *
         *  class B extends A {
         *      &#64;Override
         *      getIgnoreMe() {
         *          ...
         *      }
         *  }
         * </code>
         * </pre>
         *
         * @param target
         * @return
         */
        private boolean superClassIgnore(AnnotationTarget target) {
            ClassInfo declaringClass = TypeUtil.getDeclaringClass(target);
            AnnotationInstance declaringClassJIP = TypeUtil.getAnnotation(declaringClass, getName());
            // if overridden by subclass than superclass ignores are not merged
            if (declaringClassJIP != null) {
                return false;
            }
            ClassInfo superclassInfo = getClassInfoFromIndex(declaringClass.superClassType());
            if (superclassInfo != null) {
                AnnotationInstance superClassJIP = TypeUtil.getAnnotation(superclassInfo, getName());
                return shouldIgnoreTarget(superClassJIP, propertyName(target));
            }
            return false;
        }

        /**
         * Look for nested/enclosing type @com.fasterxml.jackson.annotation.JsonIgnoreProperties.
         *
         * <pre>
         * <code>
         * class A {
         *   &#64;com.fasterxml.jackson.annotation.JsonIgnoreProperties("ignoreMe")
         *   B foo;
         * }
         *
         * class B {
         *   String ignoreMe; // Ignored during scan via A.
         *   String doNotIgnoreMe;
         * }
         * </code>
         * </pre>
         *
         * @param nesting
         * @param propertyName
         * @return
         */
        private boolean nestingPropertyIgnore(AnnotationTarget nesting, String propertyName) {
            if (nesting == null) {
                return false;
            }
            AnnotationInstance nestedTypeJIP = TypeUtil.getAnnotation(nesting, getName());
            return shouldIgnoreTarget(nestedTypeJIP, propertyName);
        }

        private String propertyName(AnnotationTarget target) {
            if (target.kind() == Kind.FIELD) {
                return target.asField().name();
            }
            // Assuming this is a getter or setter
            String name = target.asMethod().name().substring(3);
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }

        private boolean shouldIgnoreTarget(AnnotationInstance jipAnnotation, String targetName) {
            if (jipAnnotation == null || jipAnnotation.value() == null) {
                return false;
            }
            String[] jipValues = jipAnnotation.value().asStringArray();
            return Arrays.stream(jipValues).anyMatch(v -> v.equals(targetName));
        }

        @Override
        public DotName getName() {
            return JacksonConstants.JSON_IGNORE_PROPERTIES;
        }
    }

    /**
     * Handler for Jackson's @{@link com.fasterxml.jackson.annotation.JsonIgnore JsonIgnore}
     */
    private final class JsonIgnoreHandler implements IgnoreAnnotationHandler {

        @Override
        public boolean shouldIgnore(AnnotationTarget target, DataObjectDeque.PathEntry parentPathEntry) {
            AnnotationInstance annotationInstance = TypeUtil.getAnnotation(target, getName());
            if (annotationInstance != null) {
                return valueAsBooleanOrTrue(annotationInstance);
            }
            return false;
        }

        @Override
        public DotName getName() {
            return JacksonConstants.JSON_IGNORE;
        }
    }

    /**
     * Handler for @{@link JsonIgnoreType}
     */
    private final class JsonIgnoreTypeHandler implements IgnoreAnnotationHandler {
        private final Set<DotName> ignoredTypes = new LinkedHashSet<>();

        @Override
        public boolean shouldIgnore(AnnotationTarget target, DataObjectDeque.PathEntry parentPathEntry) {
            Type classType;

            switch (target.kind()) {
                case FIELD:
                    classType = target.asField().type();
                    break;
                case METHOD:
                    MethodInfo method = target.asMethod();
                    if (method.returnType().kind().equals(Type.Kind.VOID)) {
                        // Setter method
                        classType = method.parameters().get(0);
                    } else {
                        // Getter method
                        classType = method.returnType();
                    }
                    break;
                default:
                    return false;
            }

            // Primitive and non-indexed types will result in a null
            if (classType.kind() == Type.Kind.PRIMITIVE ||
                    classType.kind() == Type.Kind.VOID ||
                    (classType.kind() == Type.Kind.ARRAY && classType.asArrayType().component().kind() == Type.Kind.PRIMITIVE)
                    ||
                    !index.containsClass(classType)) {
                return false;
            }

            // Find the real class implementation where the @JsonIgnoreType annotation may be.
            ClassInfo classInfo = index.getClass(classType);

            if (ignoredTypes.contains(classInfo.name())) {
                DataObjectLogging.log.ignoringType(classInfo.name());
                return true;
            }

            AnnotationInstance annotationInstance = TypeUtil.getAnnotation(classInfo, getName());
            if (annotationInstance != null && valueAsBooleanOrTrue(annotationInstance)) {
                // Add the ignored field or class name
                DataObjectLogging.log.ignoringTypeAndAddingToSet(classInfo.name());
                ignoredTypes.add(classInfo.name());
                return true;
            }
            return false;
        }

        @Override
        public DotName getName() {
            return JacksonConstants.JSON_IGNORE_TYPE;
        }
    }

    private final class TransientIgnoreHandler implements IgnoreAnnotationHandler {

        @Override
        public boolean shouldIgnore(AnnotationTarget target, PathEntry parentPathEntry) {
            if (target.kind() == AnnotationTarget.Kind.FIELD) {
                FieldInfo field = target.asField();
                // If field has transient modifier, e.g. `transient String foo;`, then hide it.
                if (Modifier.isTransient(field.flags())) {
                    // Unless field is annotated with @Schema to explicitly un-hide it.
                    AnnotationInstance schemaAnnotation = TypeUtil.getSchemaAnnotation(target);
                    if (schemaAnnotation != null) {
                        return JandexUtil.booleanValue(schemaAnnotation, SchemaConstant.PROP_HIDDEN).orElse(true);
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public DotName getName() {
            return DotName.createSimple(TransientIgnoreHandler.class.getName());
        }
    }

    private boolean valueAsBooleanOrTrue(AnnotationInstance annotation) {
        return Optional.ofNullable(annotation.value())
                .map(AnnotationValue::asBoolean)
                .orElse(true);
    }

    private interface IgnoreAnnotationHandler {
        boolean shouldIgnore(AnnotationTarget target, DataObjectDeque.PathEntry parentPathEntry);

        DotName getName();
    }

}
