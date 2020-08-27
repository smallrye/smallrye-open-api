package io.smallrye.openapi.jaxrs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.DotName;

/**
 * Constants related to JAX-RS
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class JaxRsConstants {

    static final DotName APPLICATION = DotName.createSimple("javax.ws.rs.core.Application");
    static final DotName APPLICATION_PATH = DotName.createSimple("javax.ws.rs.ApplicationPath");
    static final DotName PATH = DotName.createSimple("javax.ws.rs.Path");
    static final DotName PRODUCES = DotName.createSimple("javax.ws.rs.Produces");
    static final DotName CONSUMES = DotName.createSimple("javax.ws.rs.Consumes");
    static final DotName EXCEPTION_MAPPER = DotName.createSimple("javax.ws.rs.ext.ExceptionMapper");
    static final DotName QUERY_PARAM = DotName.createSimple("javax.ws.rs.QueryParam");
    static final DotName FORM_PARAM = DotName.createSimple("javax.ws.rs.FormParam");
    static final DotName COOKIE_PARAM = DotName.createSimple("javax.ws.rs.CookieParam");
    static final DotName PATH_PARAM = DotName.createSimple("javax.ws.rs.PathParam");
    static final DotName HEADER_PARAM = DotName.createSimple("javax.ws.rs.HeaderParam");
    static final DotName MATRIX_PARAM = DotName.createSimple("javax.ws.rs.MatrixParam");
    static final DotName BEAN_PARAM = DotName.createSimple("javax.ws.rs.BeanParam");
    static final DotName ASYNC_RESPONSE = DotName.createSimple("javax.ws.rs.container.AsyncResponse");
    static final DotName DEFAULT_VALUE = DotName.createSimple("javax.ws.rs.DefaultValue");
    static final DotName RESPONSE = DotName.createSimple("javax.ws.rs.core.Response");
    static final DotName PATH_SEGMENT = DotName.createSimple("javax.ws.rs.core.PathSegment");

    static final DotName REGISTER_REST_CLIENT = DotName
            .createSimple("org.eclipse.microprofile.rest.client.inject.RegisterRestClient");

    static final DotName GET = DotName.createSimple("javax.ws.rs.GET");
    static final DotName PUT = DotName.createSimple("javax.ws.rs.PUT");
    static final DotName POST = DotName.createSimple("javax.ws.rs.POST");
    static final DotName DELETE = DotName.createSimple("javax.ws.rs.DELETE");
    static final DotName HEAD = DotName.createSimple("javax.ws.rs.HEAD");
    static final DotName OPTIONS = DotName.createSimple("javax.ws.rs.OPTIONS");
    static final DotName PATCH = DotName.createSimple("javax.ws.rs.PATCH");

    static final String TO_RESPONSE_METHOD_NAME = "toResponse";

    static final Set<DotName> HTTP_METHODS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(
                    GET,
                    PUT,
                    POST,
                    DELETE,
                    HEAD,
                    OPTIONS,
                    PATCH)));

    private JaxRsConstants() {
    }

}
