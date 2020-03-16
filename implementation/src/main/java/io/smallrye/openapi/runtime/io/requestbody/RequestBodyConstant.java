package io.smallrye.openapi.runtime.io.requestbody;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to RequestBody
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#requestBodyObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class RequestBodyConstant implements Referenceable {

    public static final DotName DOTNAME_REQUESTBODY = DotName.createSimple(RequestBody.class.getName());

    public static final String PROP_NAME = "name";
    public static final String PROP_REQUIRED = "required";

    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_REQUEST_BODY = "requestBody";
    public static final String PROP_CONTENT = "content";

    private RequestBodyConstant() {
    }
}
