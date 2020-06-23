package io.smallrye.openapi.runtime.io.parameter;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.jboss.jandex.DotName;

/**
 * Constants related to Parameter
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#parameter-object">parameter-object</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ParameterConstant {

    public static final DotName DOTNAME_PARAMETER = DotName.createSimple(Parameter.class.getName());
    public static final DotName DOTNAME_PARAMETERS = DotName.createSimple(Parameters.class.getName());
    public static final String PROP_VALUE = "value";

    public static final String PROP_ALLOW_RESERVED = "allowReserved";
    public static final String PROP_IN = "in";
    public static final String PROP_HIDDEN = "hidden";

    private ParameterConstant() {
    }
}
