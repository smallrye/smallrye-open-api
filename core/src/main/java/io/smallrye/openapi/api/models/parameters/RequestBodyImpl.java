package io.smallrye.openapi.api.models.parameters;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.io.ReferenceType;

/**
 * An implementation of the {@link RequestBody} OpenAPI model interface.
 */
public class RequestBodyImpl extends ExtensibleImpl<RequestBody> implements RequestBody, ModelImpl {

    private String ref;
    private String description;
    private Content content;
    private Boolean required;

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
            ref = ReferenceType.REQUEST_BODY.referenceOf(ref);
        }
        this.ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.RequestBody#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.RequestBody#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.RequestBody#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.RequestBody#setContent(org.eclipse.microprofile.openapi.models.media.Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.RequestBody#getRequired()
     */
    @Override
    public Boolean getRequired() {
        return this.required;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.RequestBody#setRequired(java.lang.Boolean)
     */
    @Override
    public void setRequired(Boolean required) {
        this.required = required;
    }

}
