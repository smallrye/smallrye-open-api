package io.smallrye.openapi.api.models.security;

import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link SecurityScheme} OpenAPI model interface.
 */
public class SecuritySchemeImpl extends ExtensibleImpl<SecurityScheme> implements SecurityScheme, ModelImpl {

    private String ref;
    private Type type;
    private String description;
    private String name;
    private In in;
    private String scheme;
    private String bearerFormat;
    private OAuthFlows flows;
    private String openIdConnectUrl;

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#getRef()
     */
    @Override
    public String getRef() {
        return this.ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_SECURITY_SCHEME + ref;
        }
        this.ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#getType()
     */
    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#setType(org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type)
     */
    @Override
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#getIn()
     */
    @Override
    public In getIn() {
        return this.in;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#setIn(org.eclipse.microprofile.openapi.models.security.SecurityScheme.In)
     */
    @Override
    public void setIn(In in) {
        this.in = in;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#getScheme()
     */
    @Override
    public String getScheme() {
        return this.scheme;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#setScheme(java.lang.String)
     */
    @Override
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#getBearerFormat()
     */
    @Override
    public String getBearerFormat() {
        return this.bearerFormat;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#setBearerFormat(java.lang.String)
     */
    @Override
    public void setBearerFormat(String bearerFormat) {
        this.bearerFormat = bearerFormat;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#getFlows()
     */
    @Override
    public OAuthFlows getFlows() {
        return this.flows;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#setFlows(org.eclipse.microprofile.openapi.models.security.OAuthFlows)
     */
    @Override
    public void setFlows(OAuthFlows flows) {
        this.flows = flows;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#getOpenIdConnectUrl()
     */
    @Override
    public String getOpenIdConnectUrl() {
        return this.openIdConnectUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityScheme#setOpenIdConnectUrl(java.lang.String)
     */
    @Override
    public void setOpenIdConnectUrl(String openIdConnectUrl) {
        this.openIdConnectUrl = openIdConnectUrl;
    }

}
