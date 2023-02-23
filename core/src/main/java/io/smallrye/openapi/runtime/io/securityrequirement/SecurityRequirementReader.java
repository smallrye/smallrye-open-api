package io.smallrye.openapi.runtime.io.securityrequirement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Reading the Security from annotations or json
 *
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#security-requirement-object">security-requirement-object</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecurityRequirementReader {

    private SecurityRequirementReader() {
    }

    /**
     * Reads any SecurityRequirement and SecurityRequirementsSet annotations.
     *
     * @param securityRequirements Array of {@literal @}SecurityRequirement annotations
     * @param securityRequirementsSets Array of {@literal @}SecurityRequirementsSet annotation
     * @return List of SecurityRequirement models
     */
    public static Optional<List<SecurityRequirement>> readSecurityRequirements(final AnnotationValue securityRequirements,
            final AnnotationValue securityRequirementsSets) {
        if (securityRequirements == null && securityRequirementsSets == null) {
            return Optional.empty();
        }

        List<SecurityRequirement> requirements = new ArrayList<>();

        if (securityRequirements != null) {
            IoLogging.logger.annotationsArray("@SecurityRequirement");
            AnnotationInstance[] nestedArray = securityRequirements.asNestedArray();
            for (AnnotationInstance requirementAnno : nestedArray) {
                SecurityRequirement requirement = readSecurityRequirement(requirementAnno);
                if (requirement != null) {
                    requirements.add(requirement);
                }
            }
        }

        if (securityRequirementsSets != null) {
            IoLogging.logger.annotationsArray("@SecurityRequirementsSet");
            AnnotationInstance[] nestedArray = securityRequirementsSets.asNestedArray();
            for (AnnotationInstance requirementSetAnno : nestedArray) {
                SecurityRequirement requirement = readSecurityRequirementsSet(requirementSetAnno);
                if (requirement != null) {
                    requirements.add(requirement);
                }
            }
        }

        return Optional.of(requirements);
    }

    /**
     * Reads a list of {@link SecurityRequirement} OpenAPI nodes.
     *
     * @param node the json array
     * @return List of SecurityRequirement models
     */
    public static Optional<List<SecurityRequirement>> readSecurityRequirements(final JsonNode node) {
        if (node != null && node.isArray()) {
            IoLogging.logger.jsonArray("SecurityRequirement");
            List<SecurityRequirement> requirements = new ArrayList<>(node.size());
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode arrayItem : arrayNode) {
                requirements.add(readSecurityRequirement(arrayItem));
            }
            return Optional.of(requirements);
        }
        return Optional.empty();
    }

    /**
     * Reads a single SecurityRequirement annotation.
     *
     * @param annotationInstance the {@literal @}SecurityRequirement annotation
     * @return SecurityRequirement model
     */
    public static SecurityRequirement readSecurityRequirement(AnnotationInstance annotationInstance) {
        SecurityRequirement requirement = new SecurityRequirementImpl();
        addSecurityRequirement(requirement, annotationInstance);
        if (requirement.getSchemes().isEmpty()) {
            // Should only happen if the annotation was missing the required "name" property
            return null;
        } else {
            return requirement;
        }
    }

    /**
     * Reads a single SecurityRequirementsSet annotation
     *
     * @param annotationInstance the {@literal @}SecurityRequirementsSet annotation
     * @return SecurityRequirement model
     */
    public static SecurityRequirement readSecurityRequirementsSet(AnnotationInstance annotationInstance) {
        AnnotationValue value = annotationInstance.value();
        SecurityRequirement requirement = new SecurityRequirementImpl();
        if (value != null) {
            for (AnnotationInstance securityRequirementInstance : value.asNestedArray()) {
                addSecurityRequirement(requirement, securityRequirementInstance);
            }
        }
        return requirement;
    }

    private static void addSecurityRequirement(SecurityRequirement requirement, AnnotationInstance annotationInstance) {
        String name = JandexUtil.stringValue(annotationInstance, SecurityRequirementConstant.PROP_NAME);
        if (name != null) {
            Optional<List<String>> maybeScopes = JandexUtil.stringListValue(annotationInstance,
                    SecurityRequirementConstant.PROP_SCOPES);
            if (maybeScopes.isPresent()) {
                requirement.addScheme(name, maybeScopes.get());
            } else {
                requirement.addScheme(name);
            }
        }
    }

    /**
     * Reads a {@link APIResponses} OpenAPI node.
     *
     * @param node the json node
     * @return SecurityRequirement model
     */
    private static SecurityRequirement readSecurityRequirement(final JsonNode node) {
        if (node != null && node.isObject()) {

            SecurityRequirement requirement = new SecurityRequirementImpl();
            for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
                String fieldName = fieldNames.next();
                JsonNode scopesNode = node.get(fieldName);
                Optional<List<String>> maybeScopes = JsonUtil.readStringArray(scopesNode);
                if (maybeScopes.isPresent()) {
                    requirement.addScheme(fieldName, maybeScopes.get());
                } else {
                    requirement.addScheme(fieldName);
                }
            }
            return requirement;
        }
        return null;
    }

    // helper methods for scanners
    public static AnnotationInstance getSecurityRequirementsAnnotation(final AnnotationTarget target) {
        return TypeUtil.getAnnotation(target, SecurityRequirementConstant.DOTNAME_SECURITY_REQUIREMENTS);
    }

    // helper methods for scanners
    public static List<AnnotationInstance> getSecurityRequirementAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                SecurityRequirementConstant.DOTNAME_SECURITY_REQUIREMENT,
                SecurityRequirementConstant.DOTNAME_SECURITY_REQUIREMENTS);
    }

    // helper methods for scanners
    public static AnnotationInstance getSecurityRequirementsSetsAnnotation(final AnnotationTarget target) {
        return TypeUtil.getAnnotation(target, SecurityRequirementConstant.DOTNAME_SECURITY_REQUIREMENTS_SETS);
    }

    // helper methods for scanners
    public static List<AnnotationInstance> getSecurityRequirementsSetAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                SecurityRequirementConstant.DOTNAME_SECURITY_REQUIREMENTS_SET,
                SecurityRequirementConstant.DOTNAME_SECURITY_REQUIREMENTS_SETS);
    }
}
