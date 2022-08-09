package io.smallrye.openapi.runtime.io.operation;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.jandex.DotName;

/**
 * Constants related to Operation
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#operationObject">operationObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class OperationConstant {

    public static final DotName DOTNAME_OPERATION = DotName.createSimple(Operation.class.getName());

    public static final String PROP_OPERATION_ID = "operationId";
    public static final String PROP_TAGS = "tags";
    public static final String PROP_EXTENSIONS = "extensions";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_SECURITY = "security";
    public static final String PROP_SECURITY_SETS = "securitySets";
    public static final String PROP_REQUEST_BODY = "requestBody";
    public static final String PROP_PARAMETERS = "parameters";
    public static final String PROP_SERVERS = "servers";
    public static final String PROP_SUMMARY = "summary";
    public static final String PROP_DEPRECATED = "deprecated";
    public static final String PROP_CALLBACKS = "callbacks";
    public static final String PROP_HIDDEN = "hidden";
    public static final String PROP_RESPONSES = "responses";

    private OperationConstant() {
    }
}
