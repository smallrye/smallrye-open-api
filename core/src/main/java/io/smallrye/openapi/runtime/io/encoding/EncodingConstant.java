package io.smallrye.openapi.runtime.io.encoding;

/**
 * Constants related to Encoding.
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#encodingObject">encodingObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class EncodingConstant {

    static final String PROP_ALLOW_RESERVED = "allowReserved";
    static final String PROP_NAME = "name";
    static final String PROP_CONTENT_TYPE = "contentType";
    static final String PROP_HEADERS = "headers";
    static final String PROP_EXPLODE = "explode";
    static final String PROP_STYLE = "style";
    static final String PROP_ENCODING = "encoding";

    private EncodingConstant() {
    }
}
