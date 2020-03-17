package io.smallrye.openapi.runtime.io.parameter;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to Parameter
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#parameter-object
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ParameterConstant implements Referenceable {

    public static final DotName DOTNAME_PARAMETER = DotName.createSimple(Parameter.class.getName());
    public static final DotName DOTNAME_PARAMETERS = DotName.createSimple(Parameters.class.getName());
    public static final String PROP_VALUE = "value";

    static final String PROP_ALLOW_RESERVED = "allowReserved";
    static final String PROP_NAME = "name";
    static final String PROP_IN = "in";
    static final String PROP_EXAMPLE = "example";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_CONTENT = "content";
    static final String PROP_SCHEMA = "schema";
    static final String PROP_EXPLODE = "explode";
    static final String PROP_ALLOW_EMPTY_VALUE = "allowEmptyValue";
    static final String PROP_REQUIRED = "required";
    static final String PROP_DEPRECATED = "deprecated";
    static final String PROP_STYLE = "style";
    static final String PROP_EXAMPLES = "examples";
    static final String PROP_HIDDEN = "hidden";

    private ParameterConstant() {
    }
}
