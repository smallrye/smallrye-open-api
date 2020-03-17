package io.smallrye.openapi.runtime.io.info;

import org.eclipse.microprofile.openapi.models.info.Info;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.contact.ContactWriter;
import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.license.LicenseWriter;

/**
 * This write the Info to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#infoObject">infoObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class InfoWriter {
    private static final Logger LOG = Logger.getLogger(InfoWriter.class);

    private InfoWriter() {
    }

    /**
     * Writes the {@link Info} model to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    public static void writeInfo(ObjectNode parent, Info model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(DefinitionConstant.PROP_INFO);

        JsonUtil.stringProperty(node, InfoConstant.PROP_TITLE, model.getTitle());
        JsonUtil.stringProperty(node, InfoConstant.PROP_DESCRIPTION, model.getDescription());
        JsonUtil.stringProperty(node, InfoConstant.PROP_TERMS_OF_SERVICE, model.getTermsOfService());
        ContactWriter.writeContact(node, model.getContact());
        LicenseWriter.writeLicense(node, model.getLicense());
        JsonUtil.stringProperty(node, InfoConstant.PROP_VERSION, model.getVersion());
        ExtensionWriter.writeExtensions(node, model);
    }
}
