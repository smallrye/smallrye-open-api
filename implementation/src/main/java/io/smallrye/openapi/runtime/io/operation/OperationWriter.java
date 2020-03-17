package io.smallrye.openapi.runtime.io.operation;

import org.eclipse.microprofile.openapi.models.Operation;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.callback.CallbackWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsWriter;
import io.smallrye.openapi.runtime.io.parameter.ParameterWriter;
import io.smallrye.openapi.runtime.io.requestbody.RequestBodyWriter;
import io.smallrye.openapi.runtime.io.response.ResponseWriter;
import io.smallrye.openapi.runtime.io.securityrequirement.SecurityRequirementWriter;
import io.smallrye.openapi.runtime.io.server.ServerWriter;

/**
 * Writing the OperationWriter to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#operationObject">operationObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class OperationWriter {

    private OperationWriter() {
    }

    /**
     * Writes a {@link Operation} to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param method
     */
    public static void writeOperation(ObjectNode parent, Operation model, String method) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(method);
        ObjectWriter.writeStringArray(node, model.getTags(), OperationConstant.PROP_TAGS);
        JsonUtil.stringProperty(node, OperationConstant.PROP_SUMMARY, model.getSummary());
        JsonUtil.stringProperty(node, OperationConstant.PROP_DESCRIPTION, model.getDescription());
        ExternalDocsWriter.writeExternalDocumentation(node, model.getExternalDocs());
        JsonUtil.stringProperty(node, OperationConstant.PROP_OPERATION_ID, model.getOperationId());
        ParameterWriter.writeParameterList(node, model.getParameters());
        RequestBodyWriter.writeRequestBody(node, model.getRequestBody());
        ResponseWriter.writeAPIResponses(node, model.getResponses());
        CallbackWriter.writeCallbacks(node, model.getCallbacks());
        JsonUtil.booleanProperty(node, OperationConstant.PROP_DEPRECATED, model.getDeprecated());
        SecurityRequirementWriter.writeSecurityRequirements(node, model.getSecurity());
        ServerWriter.writeServers(node, model.getServers());
        ExtensionWriter.writeExtensions(node, model);
    }

}
