package io.smallrye.openapi.runtime.io.operation;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.ExternalDocumentable;

/**
 * Constants related to Operation
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#operationObject">operationObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class OperationConstant implements ExternalDocumentable {

    static final DotName DOTNAME_OPERATION = DotName.createSimple(Operation.class.getName());

    static final String PROP_OPERATION_ID = "operationId";
    static final String PROP_TAGS = "tags";
    static final String PROP_EXTENSIONS = "extensions";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_SECURITY = "security";
    static final String PROP_REQUEST_BODY = "requestBody";
    static final String PROP_PARAMETERS = "parameters";
    static final String PROP_SERVERS = "servers";
    static final String PROP_SUMMARY = "summary";
    static final String PROP_DEPRECATED = "deprecated";
    static final String PROP_CALLBACKS = "callbacks";
    static final String PROP_HIDDEN = "hidden";
    static final String PROP_RESPONSES = "responses";

    private OperationConstant() {
    }
}
