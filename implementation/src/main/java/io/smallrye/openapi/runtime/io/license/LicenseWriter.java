package io.smallrye.openapi.runtime.io.license;

import org.eclipse.microprofile.openapi.models.info.License;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.info.InfoConstant;

/**
 * This writes the License to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#licenseObject">licenseObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class LicenseWriter {
    private static final Logger LOG = Logger.getLogger(LicenseWriter.class);

    private LicenseWriter() {
    }

    /**
     * Writes the {@link License} model to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    public static void writeLicense(ObjectNode parent, License model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(InfoConstant.PROP_LICENSE);

        JsonUtil.stringProperty(node, LicenseConstant.PROP_NAME, model.getName());
        JsonUtil.stringProperty(node, LicenseConstant.PROP_URL, model.getUrl());
        ExtensionWriter.writeExtensions(node, model);
    }
}
