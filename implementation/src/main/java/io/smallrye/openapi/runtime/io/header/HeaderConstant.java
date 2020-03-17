package io.smallrye.openapi.runtime.io.header;

import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to Header
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#headerObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class HeaderConstant implements Referenceable {

    static final String PROP_NAME = "name";
    static final String PROP_EXAMPLE = "example";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_CONTENT = "content";
    static final String PROP_SCHEMA = "schema";
    static final String PROP_EXPLODE = "explode";
    static final String PROP_ALLOW_EMPTY_VALUE = "allowEmptyValue";
    static final String PROP_REQUIRED = "required";
    static final String PROP_DEPRECATED = "deprecated";
    static final String PROP_STYLE = "style";
    static final String PROP_EXAMPLES = "examples";

    private HeaderConstant() {
    }
}
