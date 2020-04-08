package io.smallrye.openapi.runtime.io.xml;

import org.eclipse.microprofile.openapi.models.media.XML;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;

/**
 * Writing the Xml to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#xmlObject">xmlObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class XmlWriter {

    private XmlWriter() {
    }

    /**
     * Writes a {@link XML} object to the JSON tree.
     * 
     * @param parent the parent json node
     * @param model the XML model
     */
    public static void writeXML(ObjectNode parent, XML model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(SchemaConstant.PROP_XML);
        JsonUtil.stringProperty(node, XmlConstant.PROP_NAME, model.getName());
        JsonUtil.stringProperty(node, XmlConstant.PROP_NAMESPACE, model.getNamespace());
        JsonUtil.stringProperty(node, XmlConstant.PROP_PREFIX, model.getPrefix());
        JsonUtil.booleanProperty(node, XmlConstant.PROP_ATTRIBUTE, model.getAttribute());
        JsonUtil.booleanProperty(node, XmlConstant.PROP_WRAPPED, model.getWrapped());
        ExtensionWriter.writeExtensions(node, model);
    }
}
