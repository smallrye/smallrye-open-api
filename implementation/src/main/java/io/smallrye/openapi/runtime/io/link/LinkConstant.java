package io.smallrye.openapi.runtime.io.link;

import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to Link
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#linkObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class LinkConstant implements Referenceable {

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
