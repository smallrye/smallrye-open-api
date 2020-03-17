package io.smallrye.openapi.runtime.io.securityscheme;

import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to SecurityScheme
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#securitySchemeObject">securitySchemeObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecuritySchemeConstant implements Referenceable {

    static final DotName DOTNAME_SECURITY_SCHEME = DotName.createSimple(SecurityScheme.class.getName());
    static final DotName TYPE_SECURITY_SCHEMES = DotName.createSimple(SecuritySchemes.class.getName());

    static final String PROP_NAME = "name";
    static final String PROP_IN = "in";
    static final String PROP_CLIENT_CREDENTIALS = "clientCredentials";
    static final String PROP_SCOPES = "scopes";
    static final String PROP_BEARER_FORMAT = "bearerFormat";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_REFRESH_URL = "refreshUrl";

    static final String PROP_TOKEN_URL = "tokenUrl";
    static final String PROP_SCHEME = "scheme";
    // OAuth
    @SuppressWarnings(value = "squid:S2068")
    static final String PROP_PASSWORD = "password";
    static final String PROP_OPEN_ID_CONNECT_URL = "openIdConnectUrl";
    static final String PROP_IMPLICIT = "implicit";
    static final String PROP_AUTHORIZATION_CODE = "authorizationCode";
    static final String PROP_SECURITY_SCHEME_NAME = "securitySchemeName";
    static final String PROP_FLOWS = "flows";
    static final String PROP_TYPE = "type";
    static final String PROP_AUTHORIZATION_URL = "authorizationUrl";
    static final String PROP_API_KEY_NAME = "apiKeyName";

    private SecuritySchemeConstant() {
    }
}
