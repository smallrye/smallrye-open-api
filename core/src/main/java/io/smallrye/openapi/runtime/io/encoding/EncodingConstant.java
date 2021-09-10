package io.smallrye.openapi.runtime.io.encoding;

/**
 * Constants related to Encoding.
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#encodingObject">encodingObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class EncodingConstant {

    public static final String PROP_ALLOW_RESERVED = "allowReserved";
    public static final String PROP_NAME = "name";
    public static final String PROP_CONTENT_TYPE = "contentType";
    public static final String PROP_HEADERS = "headers";
    public static final String PROP_EXPLODE = "explode";
    public static final String PROP_STYLE = "style";
    public static final String PROP_ENCODING = "encoding";

    private EncodingConstant() {
    }
}
