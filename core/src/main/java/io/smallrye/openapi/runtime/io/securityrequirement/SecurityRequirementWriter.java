package io.smallrye.openapi.runtime.io.securityrequirement;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;

/**
 * Writing the Security requirement to json
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#security-requirement-object">security-requirement-object</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecurityRequirementWriter {

    private SecurityRequirementWriter() {
    }

    /**
     * Writes a list of {@link SecurityRequirement} to the JSON tree.
     * 
     * @param parent the parent json node
     * @param models list of SecurityRequirement models
     */
    public static void writeSecurityRequirements(ObjectNode parent, List<SecurityRequirement> models) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray(DefinitionConstant.PROP_SECURITY);
        for (SecurityRequirement securityRequirement : models) {
            ObjectNode secNode = node.addObject();
            writeSecurityRequirement(secNode, securityRequirement);
        }
    }

    /**
     * Writes a {@link SecurityRequirement} to the given JS node.
     * 
     * @param node
     * @param model
     */
    private static void writeSecurityRequirement(ObjectNode node, SecurityRequirement model) {
        if (model == null) {
            return;
        }
        if (model.getSchemes() != null) {
            for (Map.Entry<String, List<String>> entry : model.getSchemes().entrySet()) {
                ObjectWriter.writeStringArray(node, entry.getValue(), entry.getKey());
            }
        }
    }
}
