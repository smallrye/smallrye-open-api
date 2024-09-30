package io.smallrye.openapi.runtime.scanner.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.jboss.jandex.MethodInfo;

import io.smallrye.openapi.api.constants.SecurityConstants;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * This helps to apply java security (@RolesAllowed etc.).
 *
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class JavaSecurityProcessor {

    public void addRolesAllowedToScopes(String[] roles) {
        resourceRolesAllowed = roles;
        addScopes(roles);
    }

    public void addDeclaredRolesToScopes(String[] roles) {
        addScopes(roles);
    }

    public void processSecurityRoles(MethodInfo method, Operation operation) {
        processSecurityRolesForMethodOperation(method, operation);
    }

    private final AnnotationScannerContext context;
    private String currentSecurityScheme;
    private List<OAuthFlow> currentFlows;
    private String[] resourceRolesAllowed;

    public JavaSecurityProcessor(AnnotationScannerContext context) {
        this.context = context;
    }

    public void initialize(OpenAPI openApi) {
        currentSecurityScheme = null;
        currentFlows = null;
        resourceRolesAllowed = null;
        checkSecurityScheme(openApi);
    }

    /**
     * Adds the array of roles as scopes to each of the OAuth2 flows stored previously.
     * The flows are those declared by the application in components/securitySchemes
     * using annotations where the scopes were not defined. The description of the scope
     * will be set to the role name plus the string " role".
     *
     * @param roles array of roles from either <code>@DeclareRoles</code> or
     *        <code>@RolesAllowed</code>
     */
    private void addScopes(String[] roles) {
        if (roles == null || this.currentFlows == null) {
            return;
        }

        this.currentFlows.forEach(flow -> {
            if (flow.getScopes() == null) {
                flow.setScopes(new LinkedHashMap<>());
            }
            Arrays.stream(roles).forEach(role -> flow.addScope(role, role + " role"));
        });
    }

    /**
     * Add method-level or resource-level <code>RolesAllowed</code> values as
     * scopes to the current operation.
     *
     * <ul>
     * <li>If a <code>DenyAll</code> annotation is present (and a method-level
     * <code>RolesAllowed</code> is not), the roles allowed will be set to an
     * empty array.
     *
     * <li>If none of a <code>PermitAll</code>, a <code>DenyAll</code>, and a
     * <code>RolesAllowed</code> annotation is present at the method-level, the
     * roles allowed will be set to the resource's <code>RolesAllowed</code>.
     * </ul>
     *
     * @param method the current JAX-RS method
     * @param operation the OpenAPI Operation
     */
    private void processSecurityRolesForMethodOperation(MethodInfo method, Operation operation) {
        if (this.currentSecurityScheme != null) {
            String[] rolesAllowed = context.annotations().getAnnotationValue(method, SecurityConstants.ROLES_ALLOWED);

            if (rolesAllowed != null) {
                addScopes(rolesAllowed);
                addRolesAllowed(operation, rolesAllowed);
            } else if (this.resourceRolesAllowed != null) {
                boolean denyAll = context.annotations().getAnnotation(method, SecurityConstants.DENY_ALL) != null;
                boolean permitAll = context.annotations().getAnnotation(method, SecurityConstants.PERMIT_ALL) != null;

                if (denyAll) {
                    addRolesAllowed(operation, new String[0]);
                } else if (!permitAll) {
                    addRolesAllowed(operation, this.resourceRolesAllowed);
                }
            }
        }
    }

    /**
     * Add an array of roles to the operation's security requirements.
     *
     * If no security requirements yet exists, one is created with the name of the
     * single OAUTH/OPENIDCONNECT previously defined in the OpenAPI's Components
     * section.
     *
     * Otherwise, the roles are added to only a single existing requirement
     * where the name of the requirement's scheme matches the name of the
     * single OAUTH/OPENIDCONNECT previously defined in the OpenAPI's Components
     * section.
     *
     * @param operation the OpenAPI Operation
     * @param roles a list of JAX-RS roles to use as scopes
     */
    private void addRolesAllowed(Operation operation, String[] roles) {
        List<SecurityRequirement> requirements = operation.getSecurity();

        if (requirements == null) {
            SecurityRequirement requirement = OASFactory.createSecurityRequirement();
            requirement.addScheme(currentSecurityScheme, new ArrayList<>(Arrays.asList(roles)));
            operation.setSecurity(new ArrayList<>(Arrays.asList(requirement)));
        } else if (requirements.size() == 1) {
            SecurityRequirement requirement = requirements.get(0);

            if (requirement.hasScheme(currentSecurityScheme)) {
                // The name of the declared requirement must match the scheme's name
                List<String> scopes = Stream.concat(
                        requirement.getScheme(currentSecurityScheme).stream(),
                        Arrays.stream(roles))
                        .distinct()
                        .collect(Collectors.toList());
                requirement.addScheme(currentSecurityScheme, scopes);
            }
        }
    }

    /**
     * If there is a single security scheme defined by the <code>@OpenAPIDefinition</code>
     * annotations and the scheme is OAuth2 or OpenIdConnect, any of the flows
     * where no scopes have yet been provided are eligible to have scopes
     * filled by <code>@DeclareRoles</code>/<code>@RolesAllowed</code> annotations.
     *
     * @param oai the current OpenAPI result
     */
    private void checkSecurityScheme(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            return;
        }

        Map<String, SecurityScheme> schemes = openApi.getComponents().getSecuritySchemes();

        if (schemes != null && schemes.size() == 1) {
            Map.Entry<String, SecurityScheme> scheme = schemes.entrySet().iterator().next();
            SecurityScheme.Type schemeType = scheme.getValue().getType();

            if (schemeType != null) {
                switch (schemeType) {
                    case OAUTH2:
                    case OPENIDCONNECT:
                        saveSecurityScheme(scheme.getKey(), scheme.getValue());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Saves the name of the SecurityScheme and references to any flows
     * that did not have scopes defined by the application via a component
     * defined in <code>@OpenAPIDefinition</code> annotations. The saved
     * flows may have scopes added by values discovered in <code>@RolesAllowed</code>
     * annotations during scanning.
     *
     * @param scheme the scheme to save for further role processing.
     */
    private void saveSecurityScheme(String schemeName, SecurityScheme scheme) {
        this.currentSecurityScheme = schemeName;
        this.currentFlows = new ArrayList<>();

        OAuthFlows flows = scheme.getFlows();

        if (flows != null) {
            saveFlow(flows.getAuthorizationCode());
            saveFlow(flows.getClientCredentials());
            saveFlow(flows.getImplicit());
            saveFlow(flows.getPassword());
        }
    }

    /**
     * Saves an {@link OAuthFlow} object in the list of flows for further processing.
     * Only saved if no scopes were defined by the application using annotations.
     *
     * @param flow
     */
    private void saveFlow(OAuthFlow flow) {
        if (flow != null && flow.getScopes() == null) {
            this.currentFlows.add(flow);
        }
    }
}
