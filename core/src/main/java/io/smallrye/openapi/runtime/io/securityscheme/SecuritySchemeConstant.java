package io.smallrye.openapi.runtime.io.securityscheme;

import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.jboss.jandex.DotName;

/**
 * Constants related to SecurityScheme
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#securitySchemeObject">securitySchemeObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecuritySchemeConstant {

    static final DotName DOTNAME_SECURITY_SCHEME = DotName.createSimple(SecurityScheme.class.getName());
    static final DotName TYPE_SECURITY_SCHEMES = DotName.createSimple(SecuritySchemes.class.getName());

    public static final String PROP_NAME = "name";
    public static final String PROP_CLIENT_CREDENTIALS = "clientCredentials";
    public static final String PROP_SCOPES = "scopes";
    public static final String PROP_BEARER_FORMAT = "bearerFormat";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_REFRESH_URL = "refreshUrl";
    public static final String PROP_TOKEN_URL = "tokenUrl";
    public static final String PROP_IMPLICIT = "implicit";
    public static final String PROP_AUTHORIZATION_CODE = "authorizationCode";
    public static final String PROP_FLOWS = "flows";
    public static final String PROP_AUTHORIZATION_URL = "authorizationUrl";
    @SuppressWarnings(value = "squid:S2068")
    public static final String PROP_PASSWORD = "password";

    public static final String PROP_IN = "in";
    public static final String PROP_SCHEME = "scheme";
    public static final String PROP_OPEN_ID_CONNECT_URL = "openIdConnectUrl";
    public static final String PROP_SECURITY_SCHEME_NAME = "securitySchemeName";
    public static final String PROP_TYPE = "type";
    public static final String PROP_API_KEY_NAME = "apiKeyName";

    private SecuritySchemeConstant() {
    }
}
