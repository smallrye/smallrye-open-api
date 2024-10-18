package io.smallrye.openapi.api;

import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;

/**
 * Handler interface for a platform integration layer to inspect or modify an operation.
 *
 * The resource method and class from which the operation was constructed are
 * also provided.
 *
 * @since 4.0
 */
@FunctionalInterface
public interface OperationHandler {

    static final OperationHandler DEFAULT = (o, c, m) -> {
    };

    /**
     * Callback to allow modification to an {@link Operation operation},
     * together with the associated resource class and resource method
     * associated with the operation.
     *
     * @param operation
     *        the OpenAPI operation model created from the resource
     *        class/method
     * @param resourceClass
     *        the resource class that hosts REST endpoint methods
     * @param resourceMethod
     *        resource method for a REST request. The method's declaring
     *        class may differ from the resource class. For example it may
     *        have been declared in an abstract class or interface.
     */
    void handleOperation(Operation operation, ClassInfo resourceClass, MethodInfo resourceMethod);

}
