package io.smallrye.openapi.runtime.io.securityrequirement;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;

/**
 * Writing the Security requirement to json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#security-requirement-object
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecurityRequirementWriter {

    private SecurityRequirementWriter() {
    }

    /**
     * Writes the {@link SecurityRequirement} model array to the JSON tree.
     * 
     * @param parent
     * @param security
     */
    public static void writeSecurityRequirements(ObjectNode parent, List<SecurityRequirement> security) {
        if (security == null) {
            return;
        }
        ArrayNode array = parent.putArray(DefinitionConstant.PROP_SECURITY);
        for (SecurityRequirement securityRequirement : security) {
            ObjectNode srNode = array.addObject();
            for (Map.Entry<String, List<String>> entry : securityRequirement.getSchemes().entrySet()) {
                String fieldName = entry.getKey();
                List<String> values = entry.getValue();
                ArrayNode valuesNode = srNode.putArray(fieldName);
                if (values != null) {
                    for (String value : values) {
                        valuesNode.add(value);
                    }
                }
            }
        }
    }

}
