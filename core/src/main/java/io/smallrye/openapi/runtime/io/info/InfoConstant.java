package io.smallrye.openapi.runtime.io.info;

/**
 * Constants related to Info
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#infoObject">infoObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class InfoConstant {

    public static final String PROP_TERMS_OF_SERVICE = "termsOfService";
    public static final String PROP_TITLE = "title";
    public static final String PROP_VERSION = "version";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_LICENSE = "license";
    public static final String PROP_CONTACT = "contact";

    private InfoConstant() {
    }
}
