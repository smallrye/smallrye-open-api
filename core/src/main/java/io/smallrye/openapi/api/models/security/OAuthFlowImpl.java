package io.smallrye.openapi.api.models.security;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.Scopes;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link OAuthFlow} OpenAPI model interface.
 */
public class OAuthFlowImpl extends ExtensibleImpl<OAuthFlow> implements OAuthFlow, ModelImpl {

    private String authorizationUrl;
    private String tokenUrl;
    private String refreshUrl;
    // TODO: Update type MicroProfile OpenAPI 2.0
    private Scopes scopes;

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#getAuthorizationUrl()
     */
    @Override
    public String getAuthorizationUrl() {
        return this.authorizationUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#setAuthorizationUrl(java.lang.String)
     */
    @Override
    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#getTokenUrl()
     */
    @Override
    public String getTokenUrl() {
        return this.tokenUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#setTokenUrl(java.lang.String)
     */
    @Override
    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#getRefreshUrl()
     */
    @Override
    public String getRefreshUrl() {
        return this.refreshUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#setRefreshUrl(java.lang.String)
     */
    @Override
    public void setRefreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#getScopes()
     */
    @Override
    // TODO: Update return type MicroProfile OpenAPI 2.0
    public Scopes getScopes() {
        //return ModelUtil.unmodifiableMap(this.scopes);
        return this.scopes;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#setScopes(java.util.Map)
     */
    // TODO: @Override for MicroProfile OpenAPI 2.0
    public void setScopes(Map<String, String> scopes) {
        //this.scopes = ModelUtil.replace(scopes, LinkedHashMap<String, String>::new);
        if (scopes == null) {
            this.scopes = null;
        } else {
            this.scopes = new ScopesImpl(scopes);
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#addScope(java.lang.String, java.lang.String)
     */
    // TODO: @Override for MicroProfile OpenAPI 2.0
    public OAuthFlow addScope(String scope, String description) {
        // this.scopes = ModelUtil.add(scope, description, this.scopes, LinkedHashMap<String, String>::new);
        if (scope == null) {
            return this;
        }
        if (this.scopes == null) {
            this.scopes = new ScopesImpl();
        }
        scopes.addScope(scope, description);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#removeScope(java.lang.String)
     */
    // TODO: @Override for MicroProfile OpenAPI 2.0
    public void removeScope(String scope) {
        ModelUtil.remove(this.scopes, scope);
    }

    @Override
    // TODO: Remove method for MicroProfile OpenAPI 2.0
    public void setScopes(Scopes scopes) {
        this.scopes = scopes;
    }
}
