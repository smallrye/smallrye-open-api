package io.smallrye.openapi.jaxrs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.jboss.jandex.DotName;

/**
 * Constants related to JAX-RS
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class JaxRsConstants {

    static final DotName APPLICATION = DotName.createSimple(Application.class.getName());
    static final DotName APPLICATION_PATH = DotName.createSimple(ApplicationPath.class.getName());
    static final DotName PATH = DotName.createSimple(Path.class.getName());
    static final DotName PRODUCES = DotName.createSimple(Produces.class.getName());
    static final DotName CONSUMES = DotName.createSimple(Consumes.class.getName());
    static final DotName EXCEPTION_MAPPER = DotName.createSimple(ExceptionMapper.class.getName());
    static final DotName QUERY_PARAM = DotName.createSimple(QueryParam.class.getName());
    static final DotName FORM_PARAM = DotName.createSimple(FormParam.class.getName());
    static final DotName COOKIE_PARAM = DotName.createSimple(CookieParam.class.getName());
    static final DotName PATH_PARAM = DotName.createSimple(PathParam.class.getName());
    static final DotName HEADER_PARAM = DotName.createSimple(HeaderParam.class.getName());
    static final DotName MATRIX_PARAM = DotName.createSimple(MatrixParam.class.getName());
    static final DotName BEAN_PARAM = DotName.createSimple(BeanParam.class.getName());
    static final DotName ASYNC_RESPONSE = DotName.createSimple(AsyncResponse.class.getName());
    static final DotName DEFAULT_VALUE = DotName.createSimple(DefaultValue.class.getName());
    static final DotName RESPONSE = DotName.createSimple(Response.class.getName());
    static final DotName PATH_SEGMENT = DotName.createSimple(PathSegment.class.getName());

    static final DotName GET = DotName.createSimple(GET.class.getName());
    static final DotName PUT = DotName.createSimple(PUT.class.getName());
    static final DotName POST = DotName.createSimple(POST.class.getName());
    static final DotName DELETE = DotName.createSimple(DELETE.class.getName());
    static final DotName HEAD = DotName.createSimple(HEAD.class.getName());
    static final DotName OPTIONS = DotName.createSimple(OPTIONS.class.getName());
    static final DotName PATCH = DotName.createSimple(PATCH.class.getName());

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
