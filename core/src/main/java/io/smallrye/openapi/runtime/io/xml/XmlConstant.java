package io.smallrye.openapi.runtime.io.xml;

/**
 * Constants related to XML
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#xmlObject">xmlObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class XmlConstant {

    public static final String PROP_NAME = "name";
    public static final String PROP_PREFIX = "prefix";
    public static final String PROP_NAMESPACE = "namespace";
    public static final String PROP_WRAPPED = "wrapped";
    public static final String PROP_ATTRIBUTE = "attribute";

    private XmlConstant() {
    }
}
