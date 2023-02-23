package io.smallrye.openapi.runtime.io.components;

/**
 * Constants related to Components.
 *
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#componentsObject">componentsObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ComponentsConstant {

    public static final String PROP_CALLBACKS = "callbacks";
    public static final String PROP_LINKS = "links";
    public static final String PROP_SECURITY_SCHEMES = "securitySchemes";
    public static final String PROP_HEADERS = "headers";
    public static final String PROP_REQUEST_BODIES = "requestBodies";
    public static final String PROP_EXAMPLES = "examples";
    public static final String PROP_PARAMETERS = "parameters";
    public static final String PROP_RESPONSES = "responses";
    public static final String PROP_SCHEMAS = "schemas";

    private ComponentsConstant() {
    }
}
