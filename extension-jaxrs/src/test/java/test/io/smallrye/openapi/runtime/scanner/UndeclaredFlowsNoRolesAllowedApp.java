package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;

@OpenAPIDefinition(info = @Info(title = "UndeclaredFlowsNoRolesAllowed App", version = "1.0"))
// Single scheme missing 'flows'
@SecuritySchemes(value = {
        @SecurityScheme(securitySchemeName = "oidc", type = SecuritySchemeType.OPENIDCONNECT, openIdConnectUrl = "https://example.com/auth/realms/custom_realm/.well-known/openid-configuration") })
public class UndeclaredFlowsNoRolesAllowedApp extends Application {

}
