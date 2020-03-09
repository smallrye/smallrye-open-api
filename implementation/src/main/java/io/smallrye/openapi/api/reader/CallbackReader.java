package io.smallrye.openapi.api.reader;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.api.models.callbacks.CallbackImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Callback annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#callbackObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class CallbackReader {
    private static final Logger LOG = Logger.getLogger(CallbackReader.class);

    private CallbackReader() {
    }

    /**
     * Reads a map of Callback annotations.
     * 
     * @param context the scanner context
     * @param annotationValue Map of {@literal @}Callback annotations
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return Map of Callback models
     */
    public static Map<String, Callback> readCallbacks(final AnnotationScannerContext context,
            final AnnotationValue annotationValue,
            final String[] currentConsumes,
            final String[] currentProduces) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @Callback annotations.");
        Map<String, Callback> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readCallback(context, nested, currentConsumes, currentProduces));
            }
        }
        return map;
    }

    /**
     * Reads a Callback annotation into a model.
     * 
     * @param annotation the {@literal @}Callback annotation
     * @param context the scanner context
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return Callback model
     */
    public static Callback readCallback(final AnnotationScannerContext context,
            final AnnotationInstance annotation,
            final String[] currentConsumes,
            final String[] currentProduces) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Callback annotation.");
        Callback callback = new CallbackImpl();
        callback.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.Callback));
        String expression = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_CALLBACK_URL_EXPRESSION);
        callback.addPathItem(expression,
                readCallbackOperations(context, annotation.value(OpenApiConstants.PROP_OPERATIONS), currentConsumes,
                        currentProduces));
        return callback;
    }

    /**
     * Reads the CallbackOperation annotations as a PathItem. The annotation value
     * in this case is an array of CallbackOperation annotations.
     * 
     * @param context the scanning context
     * @param annotationValue the {@literal @}CallbackOperation annotations
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     */
    private static PathItem readCallbackOperations(final AnnotationScannerContext context,
            final AnnotationValue annotationValue,
            final String[] currentConsumes,
            final String[] currentProduces) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing an array of @CallbackOperation annotations.");
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        PathItem pathItem = new PathItemImpl();
        for (AnnotationInstance operationAnno : nestedArray) {
            String method = JandexUtil.stringValue(operationAnno, OpenApiConstants.PROP_METHOD);
            Operation operation = CallbackOperationReader.readOperation(context, operationAnno, currentConsumes,
                    currentProduces);
            if (method == null) {
                continue;
            }
            try {
                PropertyDescriptor descriptor = new PropertyDescriptor(method.toUpperCase(), pathItem.getClass());
                Method mutator = descriptor.getWriteMethod();
                mutator.invoke(pathItem, operation);
            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOG.error("Error reading a CallbackOperation annotation.", e);
            }
        }
        return pathItem;
    }

}
