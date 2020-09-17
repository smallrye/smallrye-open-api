package io.smallrye.openapi.runtime.io.extension;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.extensions.Extensions;
import org.jboss.jandex.DotName;

/**
 * Constants related to Extension.
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#specificationExtensions">specificationExtensions</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExtensionConstant {

    static final DotName DOTNAME_EXTENSIONS = DotName.createSimple(Extensions.class.getName());
    static final DotName DOTNAME_EXTENSION = DotName.createSimple(Extension.class.getName());

    public static final String PROP_NAME = "name";
    public static final String PROP_VALUE = "value";
    public static final String EXTENSION_PROPERTY_PREFIX = "x-";
    public static final String EXTENSION_PROPERTY_POSTFIX = "-ext";
    public static final String PROP_PARSE_VALUE = "parseValue";

    public static boolean isExtensionField(String fieldName) {
        return fieldName.toLowerCase().startsWith(ExtensionConstant.EXTENSION_PROPERTY_PREFIX)
                || fieldName.toLowerCase().endsWith(ExtensionConstant.EXTENSION_PROPERTY_POSTFIX);
    }

    private ExtensionConstant() {
    }
}
