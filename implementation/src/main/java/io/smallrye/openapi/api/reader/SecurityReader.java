package io.smallrye.openapi.api.reader;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Security annotations
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#securityRequirementObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecurityReader {
    private static final Logger LOG = Logger.getLogger(SecurityReader.class);

    private SecurityReader() {
    }

    /**
     * Reads any SecurityRequirement annotations. The annotation value is an array of
     * SecurityRequirement annotations.
     * 
     * @param annotationValue Array of {@literal @}SecurityRequirement annotations
     * @return List of SecurityRequirement models
     */
    public static List<SecurityRequirement> readSecurity(final AnnotationValue annotationValue) {
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
     * Reads a single SecurityRequirement annotation.
     * 
     * @param annotationInstance the {@literal @}SecurityRequirement annotation
     * @return SecurityRequirement model
     */
    public static SecurityRequirement readSecurityRequirement(AnnotationInstance annotationInstance) {
        String name = JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_NAME);
        if (name != null) {
            List<String> scopes = JandexUtil.stringListValue(annotationInstance, OpenApiConstants.PROP_SCOPES);
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
}
