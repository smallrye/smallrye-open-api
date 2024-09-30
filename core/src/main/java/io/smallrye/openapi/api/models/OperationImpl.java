package io.smallrye.openapi.api.models;

import io.smallrye.openapi.internal.models.Operation;
import io.smallrye.openapi.model.Extensions;

/**
 * @deprecated use {@link org.eclipse.microprofile.openapi.OASFactory#createOperation()} instead.
 */
@Deprecated(since = "4.0", forRemoval = true)
public class OperationImpl extends Operation { // NOSONAR

    public String getMethodRef() {
        return getMethodRef(this);
    }

    public static String getMethodRef(org.eclipse.microprofile.openapi.models.Operation operation) {
        return Extensions.getMethodRef(operation);
    }

}
