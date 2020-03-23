package io.smallrye.openapi.api.models;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;

/**
 * An implementation of the {@link ExternalDocumentation} OpenAPI model interface.
 */
public class ExternalDocumentationImpl extends ExtensibleImpl<ExternalDocumentation>
        implements ExternalDocumentation, ModelImpl {

    private String description;
    private String url;

    /**
     * @see org.eclipse.microprofile.openapi.models.ExternalDocumentation#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.ExternalDocumentation#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.ExternalDocumentation#getUrl()
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.ExternalDocumentation#setUrl(java.lang.String)
     */
    @Override
    public void setUrl(String url) {
        this.url = url;
    }

}
