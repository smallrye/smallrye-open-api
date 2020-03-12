package io.smallrye.openapi.api.models.info;

import org.eclipse.microprofile.openapi.models.info.Contact;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link Contact} OpenAPI model interface.
 */
public class ContactImpl extends ExtensibleImpl<Contact> implements Contact, ModelImpl {

    private String name;
    private String url;
    private String email;

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#getUrl()
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#setUrl(java.lang.String)
     */
    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#getEmail()
     */
    @Override
    public String getEmail() {
        return this.email;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#setEmail(java.lang.String)
     */
    @Override
    public void setEmail(String email) {
        this.email = email;
    }

}
