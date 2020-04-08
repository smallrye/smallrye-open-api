package io.smallrye.openapi.runtime.io.link;

/**
 * Constants related to Link
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#linkObject">linkObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class LinkConstant {

    static final String PROP_OPERATION_ID = "operationId";
    static final String PROP_PARAMETERS = "parameters";
    static final String PROP_NAME = "name";
    static final String PROP_OPERATION_REF = "operationRef";
    static final String PROP_SERVER = "server";
    static final String PROP_EXPRESSION = "expression";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_REQUEST_BODY = "requestBody";

    private LinkConstant() {
    }
}
