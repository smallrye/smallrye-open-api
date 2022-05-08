package io.smallrye.openapi.api.models.tags;

import java.util.Objects;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.tags.Tag;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link Tag} OpenAPI model interface.
 */
public class TagImpl extends ExtensibleImpl<Tag> implements Tag, ModelImpl {

    private String name;
    private String description;
    private ExternalDocumentation externalDocs;

    /**
     * @see org.eclipse.microprofile.openapi.models.tags.Tag#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.tags.Tag#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.tags.Tag#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.tags.Tag#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.tags.Tag#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.tags.Tag#setExternalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TagImpl tag = (TagImpl) o;
        return Objects.equals(name, tag.name)
                && Objects.equals(description, tag.description)
                && Objects.equals(externalDocs, tag.externalDocs)
                && Objects.equals(getExtensions(), tag.getExtensions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, externalDocs, getExtensions());
    }
}
