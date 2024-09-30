package io.smallrye.openapi.internal.models.parameters;

import io.smallrye.openapi.model.Extensions;

public class RequestBody extends AbstractRequestBody {

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getRequired() {
        if (Extensions.getIsRequiredSet(this)) {
            return super.getRequired();
        } else {
            return Extensions.getRequiredDefault(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRequired(Boolean required) {
        super.setRequired(required);
        Extensions.setIsRequiredSet(this, Boolean.TRUE);
    }
}
