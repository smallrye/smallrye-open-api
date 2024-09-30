@OASModelType(name = "OAuthFlow", constructible = org.eclipse.microprofile.openapi.models.security.OAuthFlow.class, properties = {
        @OASModelProperty(name = "authorizationUrl", type = String.class),
        @OASModelProperty(name = "tokenUrl", type = String.class),
        @OASModelProperty(name = "refreshUrl", type = String.class),
        @OASModelProperty(name = "scopes", type = Map.class, valueType = String.class),
})
@OASModelType(name = "OAuthFlows", constructible = org.eclipse.microprofile.openapi.models.security.OAuthFlows.class, properties = {
        @OASModelProperty(name = "implicit", type = org.eclipse.microprofile.openapi.models.security.OAuthFlow.class),
        @OASModelProperty(name = "password", type = org.eclipse.microprofile.openapi.models.security.OAuthFlow.class),
        @OASModelProperty(name = "clientCredentials", type = org.eclipse.microprofile.openapi.models.security.OAuthFlow.class),
        @OASModelProperty(name = "authorizationCode", type = org.eclipse.microprofile.openapi.models.security.OAuthFlow.class),
})
@OASModelType(name = "AbstractSecurityRequirement", incomplete = true, constructible = org.eclipse.microprofile.openapi.models.security.SecurityRequirement.class, properties = {
        @OASModelProperty(name = "schemes", unwrapped = true, type = Map.class, valueTypeLiteral = "java.util.List<String>"),
})
@OASModelType(name = "SecurityScheme", constructible = org.eclipse.microprofile.openapi.models.security.SecurityScheme.class, properties = {
        @OASModelProperty(name = "ref", type = String.class),
        @OASModelProperty(name = "type", type = org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "name", type = String.class),
        @OASModelProperty(name = "in", type = org.eclipse.microprofile.openapi.models.security.SecurityScheme.In.class),
        @OASModelProperty(name = "scheme", type = String.class),
        @OASModelProperty(name = "bearerFormat", type = String.class),
        @OASModelProperty(name = "flows", type = org.eclipse.microprofile.openapi.models.security.OAuthFlows.class),
        @OASModelProperty(name = "openIdConnectUrl", type = String.class),
})
package io.smallrye.openapi.internal.models.security;

import java.util.Map;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
