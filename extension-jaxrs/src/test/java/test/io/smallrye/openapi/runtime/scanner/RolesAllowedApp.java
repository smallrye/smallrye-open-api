package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.OAuthScope;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(info = @Info(title = "RolesAllowed App", version = "1.0"), components = @Components(securitySchemes = {
        @SecurityScheme(securitySchemeName = "rolesScheme", type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(clientCredentials = @OAuthFlow, implicit = @OAuthFlow(scopes = {
                @OAuthScope(name = "scope1", description = "Provided by OAI annotation") }))) }))
public class RolesAllowedApp extends Application {

}
