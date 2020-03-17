package io.smallrye.openapi.runtime.io.securityrequirement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Security from annotations or json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#security-requirement-object">security-requirement-object</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecurityRequirementReader {
    private static final Logger LOG = Logger.getLogger(SecurityRequirementReader.class);

    private SecurityRequirementReader() {
    }

    /**
     * Reads any SecurityRequirement annotations. The annotation value is an array of
     * SecurityRequirement annotations.
     * 
     * @param annotationValue Array of {@literal @}SecurityRequirement annotations
     * @return List of SecurityRequirement models
     */
    public static List<SecurityRequirement> readSecurityRequirements(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing an array of @SecurityRequirement annotations.");
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        List<SecurityRequirement> requirements = new ArrayList<>();
        for (AnnotationInstance requirementAnno : nestedArray) {
            SecurityRequirement requirement = readSecurityRequirement(requirementAnno);
            if (requirement != null) {
                requirements.add(requirement);
            }
        }
        return requirements;
    }

    /**
     * Reads a list of {@link SecurityRequirement} OpenAPI nodes.
     * 
     * @param node the json array
     * @return List of SecurityRequirement models
     */
    public static List<SecurityRequirement> readSecurityRequirements(final JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        LOG.debug("Processing a json array of SecurityRequirement.");
        List<SecurityRequirement> requirements = new ArrayList<>(node.size());
        ArrayNode arrayNode = (ArrayNode) node;
        for (JsonNode arrayItem : arrayNode) {
            requirements.add(readSecurityRequirement(arrayItem));
        }
        return requirements;
    }

    /**
     * Reads a single SecurityRequirement annotation.
     * 
     * @param annotationInstance the {@literal @}SecurityRequirement annotation
     * @return SecurityRequirement model
     */
    public static SecurityRequirement readSecurityRequirement(AnnotationInstance annotationInstance) {
        String name = JandexUtil.stringValue(annotationInstance, SecurityRequirementConstant.PROP_NAME);
        if (name != null) {
            List<String> scopes = JandexUtil.stringListValue(annotationInstance,
                    SecurityRequirementConstant.PROP_SCOPES);
            SecurityRequirement requirement = new SecurityRequirementImpl();
            if (scopes == null) {
                requirement.addScheme(name);
            } else {
                requirement.addScheme(name, scopes);
            }
            return requirement;
        }
        return null;
    }

    /**
     * Reads a {@link APIResponses} OpenAPI node.
     * 
     * @param node the json node
     * @return SecurityRequirement model
     */
    private static SecurityRequirement readSecurityRequirement(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        SecurityRequirement requirement = new SecurityRequirementImpl();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode scopesNode = node.get(fieldName);
            List<String> scopes = JsonUtil.readStringArray(scopesNode);
            if (scopes == null) {
                requirement.addScheme(fieldName);
            } else {
                requirement.addScheme(fieldName, scopes);
            }
        }
        return requirement;
    }

    // helper methods for scanners
    public static List<AnnotationInstance> getSecurityRequirementAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                SecurityRequirementConstant.DOTNAME_SECURITY_REQUIREMENT,
                SecurityRequirementConstant.DOTNAME_SECURITY_REQUIREMENTS);
    }
}
