package io.smallrye.openapi.runtime.io.xml;

import org.eclipse.microprofile.openapi.models.media.XML;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.media.XMLImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;

/**
 * Reading the Xml from annotation or json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#xmlObject">xmlObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class XmlReader {
    private static final Logger LOG = Logger.getLogger(XmlReader.class);

    private XmlReader() {
    }

    /**
     * Reads a {@link XML} OpenAPI node.
     * 
     * @param node the json node
     * @return XML model
     */
    public static XML readXML(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        XML xml = new XMLImpl();
        xml.setName(JsonUtil.stringProperty(node, XmlConstant.PROP_NAME));
        xml.setNamespace(JsonUtil.stringProperty(node, XmlConstant.PROP_NAMESPACE));
        xml.setPrefix(JsonUtil.stringProperty(node, XmlConstant.PROP_PREFIX));
        xml.setAttribute(JsonUtil.booleanProperty(node, XmlConstant.PROP_ATTRIBUTE));
        xml.setWrapped(JsonUtil.booleanProperty(node, XmlConstant.PROP_WRAPPED));
        ExtensionReader.readExtensions(node, xml);
        return xml;
    }

}
