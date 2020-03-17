package io.smallrye.openapi.runtime.io.xml;

/**
 * Constants related to XML
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#xmlObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class XmlConstant {

    static final String PROP_NAME = "name";
    static final String PROP_PREFIX = "prefix";
    static final String PROP_NAMESPACE = "namespace";
    static final String PROP_WRAPPED = "wrapped";
    static final String PROP_ATTRIBUTE = "attribute";

    private XmlConstant() {
    }
}
