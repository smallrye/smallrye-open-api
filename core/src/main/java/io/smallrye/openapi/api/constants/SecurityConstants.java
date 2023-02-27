package io.smallrye.openapi.api.constants;

import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.DotName;

/**
 * Constants related to the Security annotations
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecurityConstants {

    public static final List<DotName> DECLARE_ROLES = Arrays.asList(
            DotName.createSimple("javax.annotation.security.DeclareRoles"),
            DotName.createSimple("jakarta.annotation.security.DeclareRoles"));
    public static final List<DotName> ROLES_ALLOWED = Arrays.asList(
            DotName.createSimple("javax.annotation.security.RolesAllowed"),
            DotName.createSimple("jakarta.annotation.security.RolesAllowed"));
    public static final List<DotName> PERMIT_ALL = Arrays.asList(
            DotName.createSimple("javax.annotation.security.PermitAll"),
            DotName.createSimple("jakarta.annotation.security.PermitAll"));
    public static final List<DotName> DENY_ALL = Arrays.asList(
            DotName.createSimple("javax.annotation.security.DenyAll"),
            DotName.createSimple("jakarta.annotation.security.DenyAll"));

    private SecurityConstants() {
    }
}
