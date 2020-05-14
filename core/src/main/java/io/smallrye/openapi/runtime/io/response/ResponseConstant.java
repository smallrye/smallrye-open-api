package io.smallrye.openapi.runtime.io.response;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.jandex.DotName;

/**
 * Constants related to Response
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#responseObject">responseObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ResponseConstant {

    static final String PROP_RESPONSE_CODE = "responseCode";
    static final String PROP_RESPONSE_DESCRIPTION = "responseDescription";

    static final DotName DOTNAME_API_RESPONSE = DotName.createSimple(APIResponse.class.getName());
    static final DotName DOTNAME_API_RESPONSES = DotName.createSimple(APIResponses.class.getName());
    // TODO: Use class/import for MicroProfile OpenAPI 2.0
    //static final DotName DOTNAME_API_RESPONSE_SCHEMA = DotName.createSimple(APIResponseSchema.class.getName());
    static final DotName DOTNAME_API_RESPONSE_SCHEMA = DotName
            .createSimple("org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema");

    static final String PROP_NAME = "name";
    static final String PROP_HEADERS = "headers";
    static final String PROP_LINKS = "links";
    static final String PROP_DEFAULT = "default";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_CONTENT = "content";
    static final String PROP_VALUE = "value";

    private ResponseConstant() {
    }
}
