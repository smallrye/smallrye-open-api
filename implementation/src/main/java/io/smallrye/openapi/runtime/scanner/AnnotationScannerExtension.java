package io.smallrye.openapi.runtime.scanner;

import java.util.Collection;

import org.eclipse.microprofile.openapi.models.parameters.Parameter.In;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.util.JandexUtil.JaxRsParameterInfo;

/**
 * Extension point for supporting extensions to JAX-RS. Implement this directly or extend
 * {@link DefaultAnnotationScannerExtension}.
 *
 * @see DefaultAnnotationScannerExtension
 */
@SuppressWarnings("deprecation")
public interface AnnotationScannerExtension {

    /**
     * Determines where an @Parameter can be found (examples include Query,
     * Path, Header, Cookie, etc).
     *
     * @param paramInfo
     * @return the parameter location, or null if unknown
     */
    @Deprecated
    public In parameterIn(MethodParameterInfo paramInfo);

    /**
     * Returns jax-rs info about the parameter at the given index. If the index
     * is invalid or does not refer to a jax-rs parameter then this should
     * return null. Otherwise it will return a {@link JaxRsParameterInfo} object
     * with the name and type of the param.
     *
     * @param method
     *        MethodInfo
     * @param idx
     *        index of parameter
     * @return JaxRsParameterInfo or null if unknown.
     */
    @Deprecated
    public JaxRsParameterInfo getMethodParameterJaxRsInfo(MethodInfo method, int idx);

    /**
     * Unwraps an asynchronous type such as
     * <code>CompletionStage&lt;X&gt;</code> into its resolved type
     * <code>X</code>
     *
     * @param type
     *        the type to unwrap if it is a supported async type
     * @return the resolved type or null if not supported
     */
    public Type resolveAsyncType(Type type);

    /**
     * Gives a chance to extensions to process the set of jax-rs application classes.
     * 
     * @param scanner the scanner used for application scanning
     * @param applications the set of jax-rs application classes
     */
    public void processJaxRsApplications(OpenApiAnnotationScanner scanner, Collection<ClassInfo> applications);

    /**
     * Returns true if the given annotation is a jax-rs annotation extension, such as would be in the javax.ws.rs
     * package.
     * 
     * @param instance the annotation to check
     * @return true if the given annotation is a jax-rs annotation extension
     */
    public boolean isJaxRsAnnotationExtension(AnnotationInstance instance);

}
