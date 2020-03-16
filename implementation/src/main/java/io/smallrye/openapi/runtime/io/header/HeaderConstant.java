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

    public static final String PROP_HEADERS = "headers";
    public static final String PROP_NAME = "name";
    public static final String PROP_EXAMPLE = "example";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_CONTENT = "content";
    public static final String PROP_SCHEMA = "schema";
    public static final String PROP_EXPLODE = "explode";
    public static final String PROP_ALLOW_EMPTY_VALUE = "allowEmptyValue";
    public static final String PROP_REQUIRED = "required";
    public static final String PROP_DEPRECATED = "deprecated";
    public static final String PROP_STYLE = "style";
    public static final String PROP_EXAMPLES = "examples";

    private HeaderConstant() {
    }
}
