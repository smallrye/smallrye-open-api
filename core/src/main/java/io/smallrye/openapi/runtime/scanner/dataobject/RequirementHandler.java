package io.smallrye.openapi.runtime.scanner.dataobject;

import org.jboss.jandex.AnnotationTarget;

/**
 * Callback used by {@link BeanValidationScanner} and
 * {@link KotlinMetadataScanner} to indicate to the calling context that the
 * given target and key should be marked as a required property in a
 * context-specific way. E.g. parameters are handled differently than schemas.
 */
public interface RequirementHandler {
    void setRequired(AnnotationTarget target, String propertyKey);
}
