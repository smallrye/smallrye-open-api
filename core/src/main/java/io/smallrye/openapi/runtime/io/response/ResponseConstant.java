package io.smallrye.openapi.runtime.io.response;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.jandex.DotName;

/**
 * Constants related to Response
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#responseObject">responseObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ResponseConstant {

    public static final String PROP_RESPONSE_CODE = "responseCode";
    public static final String PROP_RESPONSE_DESCRIPTION = "responseDescription";

    static final DotName DOTNAME_API_RESPONSE = DotName.createSimple(APIResponse.class.getName());
    static final DotName DOTNAME_API_RESPONSES = DotName.createSimple(APIResponses.class.getName());
    static final DotName DOTNAME_API_RESPONSE_SCHEMA = DotName.createSimple(APIResponseSchema.class.getName());

    public static final String PROP_NAME = "name";
    public static final String PROP_HEADERS = "headers";
    public static final String PROP_LINKS = "links";
    public static final String PROP_DEFAULT = "default";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_CONTENT = "content";
    public static final String PROP_VALUE = "value";

    private ResponseConstant() {
    }
}
