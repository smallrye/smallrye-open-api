package io.smallrye.openapi.api.models.responses;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link APIResponse} OpenAPI model interface.
 */
public class APIResponseImpl extends ExtensibleImpl<APIResponse> implements APIResponse, ModelImpl {

    private String ref;
    private String description;
    private Map<String, Header> headers;
    private Content content;
    private Map<String, Link> links;
    private String responseCode;

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
            ref = ReferenceType.RESPONSE.referenceOf(ref);
        }
        this.ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#getHeaders()
     */
    @Override
    public Map<String, Header> getHeaders() {
        return ModelUtil.unmodifiableMap(this.headers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, Header> headers) {
        this.headers = ModelUtil.replace(headers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#addHeader(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.headers.Header)
     */
    @Override
    public APIResponse addHeader(String name, Header header) {
        this.headers = ModelUtil.add(name, header, this.headers);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#removeHeader(java.lang.String)
     */
    @Override
    public void removeHeader(String name) {
        ModelUtil.remove(this.headers, name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#setContent(org.eclipse.microprofile.openapi.models.media.Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#getLinks()
     */
    @Override
    public Map<String, Link> getLinks() {
        return ModelUtil.unmodifiableMap(this.links);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#setLinks(java.util.Map)
     */
    @Override
    public void setLinks(Map<String, Link> links) {
        this.links = ModelUtil.replace(links);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#addLink(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.links.Link)
     */
    @Override
    public APIResponse addLink(String name, Link link) {
        this.links = ModelUtil.add(name, link, this.links);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#removeLink(java.lang.String)
     */
    @Override
    public void removeLink(String name) {
        ModelUtil.remove(this.links, name);
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
}
