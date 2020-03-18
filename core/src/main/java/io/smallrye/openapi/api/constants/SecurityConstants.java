package io.smallrye.openapi.api.constants;

import org.jboss.jandex.DotName;

/**
 * Constants related to the Security annotations
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecurityConstants {

    public static final DotName DECLARE_ROLES = DotName
            .createSimple("javax.annotation.security.DeclareRoles");
    public static final DotName ROLES_ALLOWED = DotName
            .createSimple("javax.annotation.security.RolesAllowed");
    public static final DotName PERMIT_ALL = DotName
            .createSimple("javax.annotation.security.PermitAll");
    public static final DotName DENY_ALL = DotName
            .createSimple("javax.annotation.security.DenyAll");

    private SecurityConstants() {
    }
}
