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
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

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
            AnnotationInstance annotationInstance = TypeUtil.getAnnotation(target, getName());
            if (annotationInstance != null) {
                return true;
            }
            return false;
        }

        @Override
        public DotName getName() {
            return OpenApiConstants.DOTNAME_JSONB_TRANSIENT;
        }
    }

    /**
     * Handler for Jackson's {@link JsonIgnoreProperties}
     */
    private final class JsonIgnorePropertiesHandler implements IgnoreAnnotationHandler {

        @Override
        public boolean shouldIgnore(AnnotationTarget target, DataObjectDeque.PathEntry parentPathEntry) {

            if (target.kind() == AnnotationTarget.Kind.FIELD) {
                // First look at declaring class for @JsonIgnoreProperties
                // Then look at enclosing type.
                FieldInfo field = target.asField();
                return declaringClassIgnore(field) || nestingFieldIgnore(parentPathEntry.getAnnotationTarget(), field.name());
            } // TODO add method
            return false;
        }

        // Declaring class ignore
        //
        //  @JsonIgnoreProperties("ignoreMe")
        //  class A {
        //    String ignoreMe;
        //  }
        private boolean declaringClassIgnore(FieldInfo field) {
            AnnotationInstance declaringClassJIP = TypeUtil.getAnnotation(field.declaringClass(), getName());
            return shouldIgnoreTarget(declaringClassJIP, field.name());
        }

        // Look for nested/enclosing type @JsonIgnoreProperties.
        //
        // class A {
        //   @JsonIgnoreProperties("ignoreMe")
        //   B foo;
        // }
        //
        // class B {
        //   String ignoreMe; // Ignored during scan via A.
        //   String doNotIgnoreMe;
        // }
        private boolean nestingFieldIgnore(AnnotationTarget nesting, String fieldName) {
            if (nesting == null) {
                return false;
            }
            AnnotationInstance nestedTypeJIP = TypeUtil.getAnnotation(nesting, getName());
            return shouldIgnoreTarget(nestedTypeJIP, fieldName);
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
            return DotName.createSimple(JsonIgnoreProperties.class.getName());
        }
    }

    /**
     * Handler for Jackson's @{@link JsonIgnore}
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
            return DotName.createSimple(JsonIgnore.class.getName());
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

            if (target.kind() == AnnotationTarget.Kind.FIELD) {
                classType = target.asField().type();
            } else { // TODO add target.kind() method
                return false;
            }

            // Primitive and non-indexed types will result in a null
            if (classType.kind() == Type.Kind.PRIMITIVE ||
                    classType.kind() == Type.Kind.VOID ||
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
            return DotName.createSimple(JsonIgnoreType.class.getName());
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
