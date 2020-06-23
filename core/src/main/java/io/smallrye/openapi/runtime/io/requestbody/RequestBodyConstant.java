package io.smallrye.openapi.runtime.io.requestbody;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.jandex.DotName;

/**
 * Constants related to RequestBody
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#requestBodyObject">requestBodyObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class RequestBodyConstant {

    static final DotName DOTNAME_REQUESTBODY = DotName.createSimple(RequestBody.class.getName());
    // TODO: Use class/import for MicroProfile OpenAPI 2.0
    //static final DotName DOTNAME_REQUEST_BODY_SCHEMA = DotName.createSimple(RequestBodySchema.class.getName());
    static final DotName DOTNAME_REQUEST_BODY_SCHEMA = DotName
            .createSimple("org.eclipse.microprofile.openapi.annotations.parameters.RequestBodySchema");

    public static final String PROP_NAME = "name";
    public static final String PROP_REQUIRED = "required";

    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_REQUEST_BODY = "requestBody";
    public static final String PROP_CONTENT = "content";

    public static final String PROP_VALUE = "value";

    private RequestBodyConstant() {
    }
}
