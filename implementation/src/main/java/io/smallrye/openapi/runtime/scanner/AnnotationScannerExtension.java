package io.smallrye.openapi.runtime.scanner;

import org.eclipse.microprofile.openapi.models.parameters.Parameter.In;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.util.JandexUtil.JaxRsParameterInfo;

/**
 * Extension point for supporting extensions to JAX-RS.
 */
public class AnnotationScannerExtension {

    /**
     * Determines where an @Parameter can be found (examples include Query,
     * Path, Header, Cookie, etc).
     * 
     * @param paramInfo
     * @return the parameter location, or null if unknown
     */
    public In parameterIn(MethodParameterInfo paramInfo) {
        return null;
    }

    /**
     * Returns jax-rs info about the parameter at the given index. If the index
     * is invalid or does not refer to a jax-rs parameter then this should
     * return null. Otherwise it will return a {@link JaxRsParameterInfo} object
     * with the name and type of the param.
     * 
     * @param method
     *            MethodInfo
     * @param idx
     *            index of parameter
     * @return JaxRsParameterInfo or null if unknown.
     */
    public JaxRsParameterInfo getMethodParameterJaxRsInfo(MethodInfo method, int idx) {
        return null;
    }

    /**
     * Unwraps an asynchronous type such as
     * <code>CompletionStage&lt;X&gt;</code> into its resolved type
     * <code>X</code>
     * 
     * @param type
     *            the type to unwrap if it is a supported async type
     * @return the resolved type or null if not supported
     */
    public Type resolveAsyncType(Type type) {
        return null;
    }

}
