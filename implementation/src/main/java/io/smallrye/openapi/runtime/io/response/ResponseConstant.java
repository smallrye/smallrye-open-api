package io.smallrye.openapi.runtime.io.response;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to Response
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#responseObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ResponseConstant implements Referenceable {

    public static final DotName DOTNAME_API_RESPONSE = DotName.createSimple(APIResponse.class.getName());
    public static final DotName DOTNAME_API_RESPONSES = DotName.createSimple(APIResponses.class.getName());

    public static final String PROP_NAME = "name";
    public static final String PROP_HEADERS = "headers";
    public static final String PROP_LINKS = "links";
    public static final String PROP_DEFAULT = "default";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_RESPONSE_CODE = "responseCode";
    public static final String PROP_CONTENT = "content";
    public static final String PROP_RESPONSES = "responses";

    private ResponseConstant() {
    }
}
