package io.smallrye.openapi.api.models.security;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;

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
    private Map<String, String> scopes;

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
    public Map<String, String> getScopes() {
        return ModelUtil.unmodifiableMap(this.scopes);
    }

    /*
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#setScopes(java.util.Map)
     */
    @Override
    public void setScopes(Map<String, String> scopes) {
        this.scopes = ModelUtil.replace(scopes, LinkedHashMap<String, String>::new);
    }

    /*
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#addScope(java.lang.String, java.lang.String)
     */
    @Override
    public OAuthFlow addScope(String scope, String description) {
        this.scopes = ModelUtil.add(scope, description, this.scopes, LinkedHashMap<String, String>::new);
        return this;
    }

    /*
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#removeScope(java.lang.String)
     */
    @Override
    public void removeScope(String scope) {
        ModelUtil.remove(this.scopes, scope);
    }

}
