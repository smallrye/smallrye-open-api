package io.smallrye.openapi.runtime.io.callback;

import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callbacks;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to Callback.
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#callbackObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class CallbackConstant implements Referenceable {

    public static final DotName DOTNAME_CALLBACKS = DotName.createSimple(Callbacks.class.getName());
    public static final DotName DOTNAME_CALLBACK = DotName.createSimple(Callback.class.getName());

    public static final String PROP_CALLBACKS = "callbacks";
    public static final String PROP_NAME = "name";
    public static final String PROP_OPERATIONS = "operations";
    public static final String PROP_CALLBACK_URL_EXPRESSION = "callbackUrlExpression";

    private CallbackConstant() {
    }
}
