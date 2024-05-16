package io.smallrye.openapi.api.models.info;

import org.eclipse.microprofile.openapi.models.info.License;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link License} OpenAPI model interface.
 */
public class LicenseImpl extends ExtensibleImpl<License> implements License, ModelImpl {

    private String name;
    private String url;
    private String identifier;

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#getUrl()
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#setUrl(java.lang.String)
     */
    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#getIdentifier()
     */
    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#setIdentifier(java.lang.String)
     */
    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
