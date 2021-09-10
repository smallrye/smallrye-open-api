package io.smallrye.openapi.runtime.io.securityrequirement;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.jboss.jandex.DotName;

/**
 * Constants related to SecurityRequirement
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#security-requirement-object">security-requirement-object</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecurityRequirementConstant {

    static final DotName DOTNAME_SECURITY_REQUIREMENT = DotName.createSimple(SecurityRequirement.class.getName());
    static final DotName DOTNAME_SECURITY_REQUIREMENTS = DotName.createSimple(SecurityRequirements.class.getName());

    public static final String PROP_NAME = "name";
    public static final String PROP_SCOPES = "scopes";

    private SecurityRequirementConstant() {
    }
}
