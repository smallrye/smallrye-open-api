package io.smallrye.openapi.api;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;

import io.smallrye.openapi.api.OpenApiConfig.OperationIdStrategy;

/**
 * Interface that may be implemented to generate custom operationId values.
 *
 * Three built-in generators may be used by specifying an operationIdStrategy
 * configuration property value of METHOD, CLASS_METHOD, or
 * PACKAGE_CLASS_METHOD. Otherwise, an fully-qualified implementation name may
 * be given for a custom generator.
 *
 * @since 4.1.2
 */
@FunctionalInterface
public interface OperationIdGenerator {

    /**
     * Derive an operationId given the Jandex resource ClassInfo and MethodInfo.
     *
     * @param resourceClass
     *        the resource class. E.g. a Jakarta REST end-point class
     * @param method
     *        the resource method. E.g. a Jakarta REST end-point method.
     *        This may be declared in a class other than the resourceClass
     *        such as a parent class or interface.
     * @return value to be used for the OpenAPI operationId
     */
    String generateOperationId(ClassInfo resourceClass, MethodInfo method);

    /**
     * Loads an OperationIdGenerator instance by name. If providing a
     * fully-qualified implementation class name, the class must be loadable
     * using the provided ClassLoader. The loader is not required or used
     * when one of the built-in strategy names is provided.
     *
     * @param strategyName
     *        name of the OperationIdGenerator to load
     * @param loader
     *        ClassLoader to load a custom implementation, if necessary
     * @return the requested OperationIdGenerator
     */
    public static OperationIdGenerator load(String strategyName, ClassLoader loader) {
        final OperationIdGenerator strategy;

        switch (strategyName) {
            case OperationIdStrategy.METHOD:
                strategy = (c, m) -> m.name();
                break;
            case OperationIdStrategy.CLASS_METHOD:
                strategy = (c, m) -> c.name().withoutPackagePrefix() + "_" + m.name();
                break;
            case OperationIdStrategy.PACKAGE_CLASS_METHOD:
                strategy = (c, m) -> c.name() + "_" + m.name();
                break;
            default:
                final Class<?> strategyType;

                try {
                    strategyType = loader.loadClass(strategyName);
                    strategy = (OperationIdGenerator) strategyType.getConstructor().newInstance();
                } catch (Exception e) {
                    throw ApiMessages.msg.invalidOperationIdStrategyWithCause(strategyName, e);
                }
                break;
        }

        return strategy;
    }
}
