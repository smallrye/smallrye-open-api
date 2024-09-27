package io.smallrye.openapi.api.models.media;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.media.Encoding;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Encoding} OpenAPI model interface.
 */
public class EncodingImpl extends ExtensibleImpl<Encoding> implements Encoding, ModelImpl {

    private String contentType;
    private Map<String, Header> headers;
    private Style style;
    private Boolean explode;
    private Boolean allowReserved;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getContentType()
     */
    @Override
    public String getContentType() {
        return this.contentType;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#addHeader(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.headers.Header)
     */
    @Override
    public Encoding addHeader(String key, Header header) {
        this.headers = ModelUtil.add(key, header, this.headers);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#removeHeader(java.lang.String)
     */
    @Override
    public void removeHeader(String key) {
        ModelUtil.remove(this.headers, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getHeaders()
     */
    @Override
    public Map<String, Header> getHeaders() {
        return ModelUtil.unmodifiableMap(this.headers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, Header> headers) {
        this.headers = ModelUtil.replace(headers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getStyle()
     */
    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setStyle(org.eclipse.microprofile.openapi.models.media.Encoding.Style)
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getExplode()
     */
    @Override
    public Boolean getExplode() {
        return this.explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setExplode(java.lang.Boolean)
     */
    @Override
    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getAllowReserved()
     */
    @Override
    public Boolean getAllowReserved() {
        return this.allowReserved;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setAllowReserved(java.lang.Boolean)
     */
    @Override
    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

}
