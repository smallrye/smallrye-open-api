package io.smallrye.openapi.runtime.io.servervariable;

/**
 * Constants related to Server
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverVariableObject">serverVariableObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerVariableConstant {

    static final String PROP_ENUM = "enum";
    static final String PROP_NAME = "name";
    static final String PROP_DEFAULT_VALUE = "defaultValue";
    static final String PROP_DEFAULT = "default";
    static final String PROP_DESCRIPTION = "description";
    // for annotations (reserved words in Java)
    static final String PROP_ENUMERATION = "enumeration";

    private ServerVariableConstant() {
    }
}
