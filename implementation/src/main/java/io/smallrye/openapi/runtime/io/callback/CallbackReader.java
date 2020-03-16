package io.smallrye.openapi.runtime.io.callback;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.callbacks.CallbackImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.paths.PathsReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Callback annotation and json
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
     * @return Map of Callback models
     */
    public static Map<String, Callback> readCallbacks(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @Callback annotations.");
        Map<String, Callback> callbacks = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = getCallbackName(nested);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                callbacks.put(name, readCallback(context, nested));
            }
        }
        return callbacks;
    }

    /**
     * Reads the {@link Callback} OpenAPI nodes.
     * 
     * @param node the json node
     * @return Map of Callback models
     */
    public static Map<String, Callback> readCallbacks(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a json map of Callback nodes.");
        Map<String, Callback> callbacks = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            callbacks.put(fieldName, readCallback(childNode));
        }

        return callbacks;
    }

    /**
     * Reads a Callback annotation into a model.
     * 
     * @param annotation the {@literal @}Callback annotation
     * @param context the scanner context
     * @return Callback model
     */
    public static Callback readCallback(final AnnotationScannerContext context,
            final AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Callback annotation.");
        Callback callback = new CallbackImpl();
        callback.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.Callback));
        String expression = JandexUtil.stringValue(annotation, CallbackConstant.PROP_CALLBACK_URL_EXPRESSION);
        callback.addPathItem(expression,
                PathsReader.readPathItem(context, annotation.value(CallbackConstant.PROP_OPERATIONS)));
        return callback;
    }

    /**
     * Reads a {@link Callback} OpenAPI node.
     * 
     * @param node the json node
     * @return Callback model
     */
    private static Callback readCallback(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a single Callback json node.");
        Callback callback = new CallbackImpl();
        callback.setRef(JsonUtil.stringProperty(node, CallbackConstant.PROP_$REF));
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            if (fieldName.startsWith(ExtensionConstant.EXTENSION_PROPERTY_PREFIX)
                    || fieldName.equals(CallbackConstant.PROP_$REF)) {
                continue;
            }
            callback.addPathItem(fieldName, PathsReader.readPathItem(node.get(fieldName)));
        }
        ExtensionReader.readExtensions(node, callback);
        return callback;
    }

    // helper methods for scanners
    public static List<AnnotationInstance> getCallbackAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                CallbackConstant.DOTNAME_CALLBACK,
                CallbackConstant.DOTNAME_CALLBACKS);
    }

    public static String getCallbackName(AnnotationInstance annotation) {
        return JandexUtil.stringValue(annotation, CallbackConstant.PROP_NAME);
    }
}
