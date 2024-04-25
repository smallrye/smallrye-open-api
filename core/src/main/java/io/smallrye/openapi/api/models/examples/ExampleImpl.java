package io.smallrye.openapi.api.models.examples;

import org.eclipse.microprofile.openapi.models.examples.Example;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.io.ReferenceType;

/**
 * An implementation of the {@link Example} OpenAPI model interface.
 */
public class ExampleImpl extends ExtensibleImpl<Example> implements Example, ModelImpl {

    private String ref;
    private String summary;
    private String description;
    private Object value;
    private String externalValue;

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
            ref = ReferenceType.EXAMPLE.referenceOf(ref);
        }
        this.ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#getSummary()
     */
    @Override
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#setSummary(java.lang.String)
     */
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#getValue()
     */
    @Override
    public Object getValue() {
        return this.value;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#getExternalValue()
     */
    @Override
    public String getExternalValue() {
        return this.externalValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#setExternalValue(java.lang.String)
     */
    @Override
    public void setExternalValue(String externalValue) {
        this.externalValue = externalValue;
    }

}
