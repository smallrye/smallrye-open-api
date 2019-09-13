/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.openapi.runtime.scanner.dataobject;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.runtime.scanner.dataobject.DataObjectDeque.PathEntry;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class IgnoreResolver {

    private static final Logger LOG = Logger.getLogger(IgnoreResolver.class);
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

    /**
     * Handler for OAS hidden @{@link Schema}
     */
    private final class SchemaHiddenHandler implements IgnoreAnnotationHandler {
        @Override
        public boolean shouldIgnore(AnnotationTarget target, PathEntry parentPathEntry) {
            AnnotationInstance annotationInstance = TypeUtil.getAnnotation(target, getName());

            if (annotationInstance != null) {
                Boolean isHidden = JandexUtil.booleanValue(annotationInstance,
                        OpenApiConstants.PROP_HIDDEN);

                if (isHidden != null) {
                    return isHidden;
                }
            }

            return false;
        }

        @Override
        public DotName getName() {
            return OpenApiConstants.DOTNAME_SCHEMA;
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
            return OpenApiConstants.DOTNAME_JSONB_TRANSIENT;
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
            return OpenApiConstants.DOTNAME_JACKSON_IGNORE_PROPERTIES;
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
            return OpenApiConstants.DOTNAME_JACKSON_IGNORE;
        }
    }

    /**
     * Handler for @{@link JsonIgnoreType}
     */
    private final class JsonIgnoreTypeHandler implements IgnoreAnnotationHandler {
        private Set<DotName> ignoredTypes = new LinkedHashSet<>();

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
                LOG.debugv("Ignoring type that is member of ignore set: {0}", classInfo.name());
                return true;
            }

            AnnotationInstance annotationInstance = TypeUtil.getAnnotation(classInfo, getName());
            if (annotationInstance != null && valueAsBooleanOrTrue(annotationInstance)) {
                // Add the ignored field or class name
                LOG.debugv("Ignoring type and adding to ignore set: {0}", classInfo.name());
                ignoredTypes.add(classInfo.name());
                return true;
            }
            return false;
        }

        @Override
        public DotName getName() {
            return OpenApiConstants.DOTNAME_JACKSON_IGNORE_TYPE;
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
                        Boolean boolVal = JandexUtil.booleanValue(schemaAnnotation, OpenApiConstants.PROP_HIDDEN);
                        if (boolVal == null) {
                            return true;
                        } else {
                            return boolVal;
                        }
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
