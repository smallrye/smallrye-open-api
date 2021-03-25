package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(tags = {
        @Tag(name = "Test", description = "Cristian") }, info = @Info(title = "API - Service", version = "V1"), security = @SecurityRequirement(name = "API-Key"), components = @Components(securitySchemes = {
                @SecurityScheme(securitySchemeName = "API-Key", type = SecuritySchemeType.APIKEY, apiKeyName = "API-Key", in = SecuritySchemeIn.HEADER) }))
public class OpenAPIConfig extends Application {

}
