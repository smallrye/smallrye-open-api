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

    static final String PROP_NAME = "name";
    static final String PROP_VALUE = "value";
    static final String EXTENSION_PROPERTY_PREFIX = "x-";
    static final String PROP_PARSE_VALUE = "parseValue";

    private ExtensionConstant() {
    }
}
