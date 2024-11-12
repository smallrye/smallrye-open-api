package io.smallrye.openapi.runtime.scanner.dataobject;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.constants.JacksonConstants;
import io.smallrye.openapi.api.constants.JaxbConstants;
import io.smallrye.openapi.api.constants.JsonbConstants;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class IgnoreResolver {

    private final AnnotationScannerContext context;
    private final AugmentedIndexView index;
    private final IgnoreAnnotationHandler[] ignoreHandlers;

    public IgnoreResolver(AnnotationScannerContext context) {
        this.context = context;
        this.index = context.getAugmentedIndex();
        this.ignoreHandlers = new IgnoreAnnotationHandler[] {
                new SchemaHiddenHandler(),
                new JsonbTransientHandler(),
                new JsonIgnorePropertiesHandler(),
                new JsonIgnoreHandler(),
                new JsonIgnoreTypeHandler(),
                new TransientIgnoreHandler(),
                new JaxbAccessibilityHandler()
        };
    }

    public enum Visibility {
        IGNORED,
        EXPOSED,
        UNSET
    }

    public Visibility isIgnore(String propertyName,
            AnnotationTarget annotationTarget, AnnotationTarget reference) {
        for (IgnoreAnnotationHandler handler : ignoreHandlers) {
            Visibility v = handler.visibility(propertyName, annotationTarget, reference);

            if (v != Visibility.UNSET) {
                return v;
            }
        }
        return Visibility.UNSET;
    }

    public Visibility referenceVisibility(String propertyName,
            AnnotationTarget annotationTarget, AnnotationTarget reference) {
        for (IgnoreAnnotationHandler handler : ignoreHandlers) {
            Visibility v = handler.referenceVisibility(propertyName, annotationTarget, reference);

            if (v != Visibility.UNSET) {
                return v;
            }
        }
        return Visibility.UNSET;
    }

    /**
     * Check whether the given target contains annotations that configure the
     * visibility of the properties of the type annotated.
     *
     * This is used to only allow registration of a type if the annotation
     * target (field or method) that refers to the type's class is not annotated
     * with an annotation that alters the visibility of fields in the class.
     *
     * <p>
     * For example, in this scenario when we process `fieldB`, registration of
     * class B's schema will not occur because it's definition is altered by and
     * specific to its use in class A.
     *
     * <pre>
     * <code>
     * class A {
     *   &#64;JsonIgnoreProperties({"field2"})
     *   B fieldB;
     * }
     *
     * class B {
     *   int field1;
     *   int field2;
     * }
     * </code>
     * </pre>
     */
    public static boolean configuresVisibility(AnnotationScannerContext context, AnnotationTarget target) {
        if (target != null) {
            return context.getIgnoreResolver().configuresVisibility(target);
        }
        return false;
    }

    public boolean configuresVisibility(AnnotationTarget reference) {
        for (IgnoreAnnotationHandler handler : ignoreHandlers) {
            if (handler.configuresVisibility(reference)) {
                return true;
            }
        }
        return false;
    }

    public Visibility getDescendantVisibility(String propertyName, List<ClassInfo> descendants) {
        for (IgnoreAnnotationHandler handler : ignoreHandlers) {
            Visibility v = handler.getDescendantVisibility(propertyName, descendants);

            if (v != Visibility.UNSET) {
                return v;
            }
        }

        return Visibility.UNSET;
    }

    /**
     * Handler for OAS hidden @{@link Schema}
     */
    private final class SchemaHiddenHandler implements IgnoreAnnotationHandler {
        @Override
        public Visibility visibility(AnnotationTarget target, AnnotationTarget reference) {
            AnnotationInstance annotationInstance = context.annotations().getAnnotation(target, getNames());
            if (annotationInstance != null) {
                Boolean hidden = context.annotations().value(annotationInstance, SchemaConstant.PROP_HIDDEN);

                if (hidden != null) {
                    return hidden.booleanValue() ? Visibility.IGNORED : Visibility.EXPOSED;
                }
            }
            return Visibility.UNSET;
        }

        @Override
        public List<DotName> getNames() {
            return Arrays.asList(SchemaConstant.DOTNAME_SCHEMA);
        }
    }

    /**
     * Handler for JSON-B's @{@link javax.json.bind.annotation.JsonbTransient}
     */
    private final class JsonbTransientHandler implements IgnoreAnnotationHandler {
        @Override
        public Visibility visibility(AnnotationTarget target, AnnotationTarget reference) {
            return context.annotations().hasAnnotation(target, getNames()) ? Visibility.IGNORED : Visibility.UNSET;
        }

        @Override
        public List<DotName> getNames() {
            return JsonbConstants.JSONB_TRANSIENT;
        }
    }

    /**
     * Handler for Jackson's {@link com.fasterxml.jackson.annotation.JsonIgnoreProperties JsonIgnoreProperties}
     */
    private final class JsonIgnorePropertiesHandler implements IgnoreAnnotationHandler {

        @Override
        public Visibility visibility(String propertyName, AnnotationTarget target, AnnotationTarget reference) {
            Visibility visibility = declaringClassIgnore(propertyName, target);

            if (visibility != Visibility.UNSET) {
                return visibility;
            }

            return nestingPropertyIgnore(reference, propertyName);
        }

        @Override
        public Visibility referenceVisibility(String propertyName, AnnotationTarget target, AnnotationTarget reference) {
            return nestingPropertyIgnore(reference, propertyName);
        }

        @Override
        public boolean configuresVisibility(AnnotationTarget reference) {
            return context.annotations().hasAnnotation(reference, getNames());
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
        private Visibility declaringClassIgnore(String propertyName, AnnotationTarget target) {
            AnnotationInstance declaringClassJIP = context.annotations().getAnnotation(TypeUtil.getDeclaringClass(target),
                    getNames());
            return shouldIgnoreTarget(declaringClassJIP, propertyName);
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
        private Visibility nestingPropertyIgnore(AnnotationTarget nesting, String propertyName) {
            if (nesting == null) {
                return Visibility.UNSET;
            }
            AnnotationInstance nestedTypeJIP = context.annotations().getAnnotation(nesting, getNames());
            return shouldIgnoreTarget(nestedTypeJIP, propertyName);
        }

        private Visibility shouldIgnoreTarget(AnnotationInstance jipAnnotation, String targetName) {
            if (jipAnnotation == null || jipAnnotation.value() == null) {
                return Visibility.UNSET;
            }

            if (Arrays.asList(jipAnnotation.value().asStringArray()).contains(targetName)) {
                return Visibility.IGNORED;
            } else {
                return Visibility.EXPOSED;
            }
        }

        @Override
        public List<DotName> getNames() {
            return Arrays.asList(JacksonConstants.JSON_IGNORE_PROPERTIES);
        }

        @Override
        public Visibility getDescendantVisibility(String propertyName, List<ClassInfo> descendants) {
            for (ClassInfo descendant : descendants) {
                AnnotationInstance declaringClassJIP = context.annotations().getAnnotation(descendant, getNames());
                Visibility visibility = shouldIgnoreTarget(declaringClassJIP, propertyName);

                if (visibility != Visibility.UNSET) {
                    return visibility;
                }
            }

            return Visibility.UNSET;
        }
    }

    /**
     * Handler for Jackson's @{@link com.fasterxml.jackson.annotation.JsonIgnore JsonIgnore}
     */
    private final class JsonIgnoreHandler implements IgnoreAnnotationHandler {
        @Override
        public Visibility visibility(AnnotationTarget target, AnnotationTarget reference) {
            AnnotationInstance annotationInstance = context.annotations().getAnnotation(target, getNames());
            if (annotationInstance != null && valueAsBooleanOrTrue(annotationInstance)) {
                return Visibility.IGNORED;
            }
            return Visibility.UNSET;
        }

        @Override
        public List<DotName> getNames() {
            return Arrays.asList(JacksonConstants.JSON_IGNORE);
        }
    }

    /**
     * Handler for <code>com.fasterxml.jackson.annotation.JsonIgnoreType</code>
     */
    private final class JsonIgnoreTypeHandler implements IgnoreAnnotationHandler {
        private final Set<DotName> ignoredTypes = new LinkedHashSet<>();

        @Override
        public Visibility visibility(AnnotationTarget target, AnnotationTarget reference) {
            Type classType;

            switch (target.kind()) {
                case FIELD:
                    classType = target.asField().type();
                    break;
                case METHOD:
                    MethodInfo method = target.asMethod();
                    if (method.returnType().kind().equals(Type.Kind.VOID)) {
                        if (method.parameterTypes().isEmpty()) {
                            // Constructor or other method without type information
                            return Visibility.IGNORED;
                        } else {
                            // Setter method
                            classType = method.parameterType(0);
                        }
                    } else {
                        // Getter method
                        classType = method.returnType();
                    }
                    break;
                default:
                    return Visibility.UNSET;
            }

            // Primitive and non-indexed types will result in a null
            if (classType.kind() == Type.Kind.PRIMITIVE ||
                    classType.kind() == Type.Kind.VOID ||
                    (classType.kind() == Type.Kind.ARRAY && classType.asArrayType().constituent().kind() == Type.Kind.PRIMITIVE)
                    ||
                    !index.containsClass(classType)) {
                return Visibility.UNSET;
            }

            // Find the real class implementation where the @JsonIgnoreType annotation may be.
            ClassInfo classInfo = index.getClass(classType);

            if (ignoredTypes.contains(classInfo.name())) {
                DataObjectLogging.logger.ignoringType(classInfo.name());
                return Visibility.IGNORED;
            }

            AnnotationInstance annotationInstance = context.annotations().getAnnotation(classInfo, getNames());
            if (annotationInstance != null && valueAsBooleanOrTrue(annotationInstance)) {
                // Add the ignored field or class name
                DataObjectLogging.logger.ignoringTypeAndAddingToSet(classInfo.name());
                ignoredTypes.add(classInfo.name());
                return Visibility.IGNORED;
            }
            return Visibility.UNSET;
        }

        @Override
        public List<DotName> getNames() {
            return Arrays.asList(JacksonConstants.JSON_IGNORE_TYPE);
        }
    }

    private final class TransientIgnoreHandler implements IgnoreAnnotationHandler {
        @Override
        public Visibility visibility(AnnotationTarget target, AnnotationTarget reference) {
            if (target.kind() == AnnotationTarget.Kind.FIELD) {
                FieldInfo field = target.asField();
                // If field has transient modifier, e.g. `transient String foo;`, then hide it.
                if (Modifier.isTransient(field.flags())) {
                    // Unless field is annotated with @Schema to explicitly un-hide it.
                    AnnotationInstance schemaAnnotation = TypeUtil.getSchemaAnnotation(context, target);
                    if (schemaAnnotation != null) {
                        Boolean hidden = context.annotations().value(schemaAnnotation, SchemaConstant.PROP_HIDDEN);
                        if (hidden != null && !hidden) {
                            return Visibility.EXPOSED;
                        }
                    }
                    return Visibility.IGNORED;
                }
            }
            return Visibility.UNSET;
        }

        @Override
        public List<DotName> getNames() {
            return Arrays.asList(DotName.createSimple(TransientIgnoreHandler.class.getName()));
        }
    }

    private final class JaxbAccessibilityHandler implements IgnoreAnnotationHandler {
        @Override
        public Visibility visibility(AnnotationTarget target, AnnotationTarget reference) {
            if (hasXmlTransient(target)) {
                return Visibility.IGNORED;
            }

            final String accessTypeRequired;
            final ClassInfo declaringClass;
            final int flags;

            switch (target.kind()) {
                case FIELD:
                    FieldInfo field = target.asField();
                    accessTypeRequired = "FIELD";
                    declaringClass = field.declaringClass();
                    flags = field.flags();
                    break;
                case METHOD:
                    MethodInfo method = target.asMethod();
                    accessTypeRequired = "PROPERTY";
                    declaringClass = method.declaringClass();
                    flags = method.flags();
                    break;
                default:
                    return Visibility.UNSET;
            }

            Visibility result;

            if (hasXmlTransient(declaringClass)) {
                result = Visibility.IGNORED;
            } else if (isXmlExposed(target)) {
                result = Visibility.EXPOSED;
            } else {
                result = getXmlVisibility(declaringClass, accessTypeRequired, flags);
            }

            return result;
        }

        boolean hasXmlTransient(AnnotationTarget target) {
            return context.annotations().hasAnnotation(target, JaxbConstants.XML_TRANSIENT);
        }

        boolean isXmlExposed(AnnotationTarget target) {
            return context.annotations().hasAnnotation(target, JaxbConstants.XML_ALL_BINDINGS);
        }

        Visibility getXmlVisibility(ClassInfo declaringClass, String accessTypeRequired, int flags) {
            String xmlAccessType = context.annotations().getAnnotationValue(declaringClass, JaxbConstants.XML_ACCESSOR_TYPE);

            if (xmlAccessType == null) {
                return Visibility.UNSET;
            }

            if (accessTypeRequired.equals(xmlAccessType)
                    || ("PUBLIC_MEMBER".equals(xmlAccessType) && Modifier.isPublic(flags))) {
                return Visibility.EXPOSED;
            }

            return Visibility.IGNORED;
        }
    }

    private boolean valueAsBooleanOrTrue(AnnotationInstance annotation) {
        return Optional.ofNullable(annotation.value())
                .map(AnnotationValue::asBoolean)
                .orElse(true);
    }

    private interface IgnoreAnnotationHandler {
        default Visibility visibility(String propertyName, AnnotationTarget target, AnnotationTarget reference) {
            return visibility(target, reference);
        }

        default Visibility visibility(AnnotationTarget target, AnnotationTarget reference) {
            return Visibility.UNSET;
        }

        default Visibility referenceVisibility(String propertyName, AnnotationTarget target, AnnotationTarget reference) {
            return Visibility.UNSET;
        }

        /**
         * Checks whether the given reference (method/field) specifies visible/ignored
         * properties on the type that is referenced.
         */
        default boolean configuresVisibility(AnnotationTarget reference) {
            return false;
        }

        default List<DotName> getNames() {
            return Collections.emptyList();
        }

        default Visibility getDescendantVisibility(String propertyName, List<ClassInfo> descendants) {
            return Visibility.UNSET;
        }
    }

}
