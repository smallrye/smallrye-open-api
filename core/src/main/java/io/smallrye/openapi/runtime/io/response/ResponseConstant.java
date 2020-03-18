package io.smallrye.openapi.runtime.io.response;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to Response
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#responseObject">responseObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ResponseConstant implements Referenceable {

    public static final String PROP_RESPONSE_CODE = "responseCode";

    static final DotName DOTNAME_API_RESPONSE = DotName.createSimple(APIResponse.class.getName());
    static final DotName DOTNAME_API_RESPONSES = DotName.createSimple(APIResponses.class.getName());

    static final String PROP_NAME = "name";
    static final String PROP_HEADERS = "headers";
    static final String PROP_LINKS = "links";
    static final String PROP_DEFAULT = "default";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_CONTENT = "content";

    private ResponseConstant() {
    }
}
