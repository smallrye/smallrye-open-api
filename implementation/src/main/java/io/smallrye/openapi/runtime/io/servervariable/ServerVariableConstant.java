package io.smallrye.openapi.runtime.io.servervariable;

/**
 * Constants related to Server
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverVariableObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerVariableConstant {

    public static final String PROP_ENUM = "enum";
    public static final String PROP_NAME = "name";
    public static final String PROP_DEFAULT_VALUE = "defaultValue";
    public static final String PROP_DEFAULT = "default";
    public static final String PROP_DESCRIPTION = "description";
    // for annotations (reserved words in Java)
    public static final String PROP_ENUMERATION = "enumeration";

    private ServerVariableConstant() {
    }
}
