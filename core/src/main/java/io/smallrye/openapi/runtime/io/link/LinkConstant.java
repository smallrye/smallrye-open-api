package io.smallrye.openapi.runtime.io.link;

/**
 * Constants related to Link
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#linkObject">linkObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class LinkConstant {

    public static final String PROP_OPERATION_ID = "operationId";
    public static final String PROP_PARAMETERS = "parameters";
    public static final String PROP_NAME = "name";
    public static final String PROP_OPERATION_REF = "operationRef";
    public static final String PROP_SERVER = "server";
    public static final String PROP_EXPRESSION = "expression";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_REQUEST_BODY = "requestBody";

    private LinkConstant() {
    }
}
