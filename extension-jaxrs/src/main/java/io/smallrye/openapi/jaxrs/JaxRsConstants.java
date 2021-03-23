package io.smallrye.openapi.jaxrs;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.DotName;

/**
 * Constants related to JAX-RS
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class JaxRsConstants {

    static final List<DotName> APPLICATION = Arrays.asList(
            DotName.createSimple("javax.ws.rs.core.Application"),
            DotName.createSimple("jakarta.ws.rs.core.Application"));
    static final List<DotName> APPLICATION_PATH = Arrays.asList(
            DotName.createSimple("javax.ws.rs.ApplicationPath"),
            DotName.createSimple("jakarta.ws.rs.ApplicationPath"));
    static final List<DotName> PATH = Arrays.asList(
            DotName.createSimple("javax.ws.rs.Path"),
            DotName.createSimple("jakarta.ws.rs.Path"));
    static final List<DotName> PRODUCES = Arrays.asList(
            DotName.createSimple("javax.ws.rs.Produces"),
            DotName.createSimple("jakarta.ws.rs.Produces"));
    static final List<DotName> CONSUMES = Arrays.asList(
            DotName.createSimple("javax.ws.rs.Consumes"),
            DotName.createSimple("jakarta.ws.rs.Consumes"));
    static final List<DotName> EXCEPTION_MAPPER = Arrays.asList(
            DotName.createSimple("javax.ws.rs.ext.ExceptionMapper"),
            DotName.createSimple("jakarta.ws.rs.ext.ExceptionMapper"));
    static final List<DotName> QUERY_PARAM = Arrays.asList(
            DotName.createSimple("javax.ws.rs.QueryParam"),
            DotName.createSimple("jakarta.ws.rs.QueryParam"));
    static final List<DotName> FORM_PARAM = Arrays.asList(
            DotName.createSimple("javax.ws.rs.FormParam"),
            DotName.createSimple("jakarta.ws.rs.FormParam"));
    static final List<DotName> COOKIE_PARAM = Arrays.asList(
            DotName.createSimple("javax.ws.rs.CookieParam"),
            DotName.createSimple("jakarta.ws.rs.CookieParam"));
    static final List<DotName> PATH_PARAM = Arrays.asList(
            DotName.createSimple("javax.ws.rs.PathParam"),
            DotName.createSimple("jakarta.ws.rs.PathParam"));
    static final List<DotName> HEADER_PARAM = Arrays.asList(
            DotName.createSimple("javax.ws.rs.HeaderParam"),
            DotName.createSimple("jakarta.ws.rs.HeaderParam"));
    static final List<DotName> MATRIX_PARAM = Arrays.asList(
            DotName.createSimple("javax.ws.rs.MatrixParam"),
            DotName.createSimple("jakarta.ws.rs.MatrixParam"));
    static final List<DotName> BEAN_PARAM = Arrays.asList(
            DotName.createSimple("javax.ws.rs.BeanParam"),
            DotName.createSimple("jakarta.ws.rs.BeanParam"));
    static final List<DotName> ASYNC_RESPONSE = Arrays.asList(
            DotName.createSimple("javax.ws.rs.container.AsyncResponse"),
            DotName.createSimple("jakarta.ws.rs.container.AsyncResponse"));
    static final List<DotName> DEFAULT_VALUE = Arrays.asList(
            DotName.createSimple("javax.ws.rs.DefaultValue"),
            DotName.createSimple("jakarta.ws.rs.DefaultValue"));
    static final List<DotName> RESPONSE = Arrays.asList(
            DotName.createSimple("javax.ws.rs.core.Response"),
            DotName.createSimple("jakarta.ws.rs.core.Response"));
    static final List<DotName> PATH_SEGMENT = Arrays.asList(
            DotName.createSimple("javax.ws.rs.core.PathSegment"),
            DotName.createSimple("jakarta.ws.rs.core.PathSegment"));

    static final DotName REGISTER_REST_CLIENT = DotName
            .createSimple("org.eclipse.microprofile.rest.client.inject.RegisterRestClient");

    static final List<DotName> GET = Arrays.asList(
            DotName.createSimple("javax.ws.rs.GET"),
            DotName.createSimple("jakarta.ws.rs.GET"));
    static final List<DotName> PUT = Arrays.asList(
            DotName.createSimple("javax.ws.rs.PUT"),
            DotName.createSimple("jakarta.ws.rs.PUT"));
    static final List<DotName> POST = Arrays.asList(
            DotName.createSimple("javax.ws.rs.POST"),
            DotName.createSimple("jakarta.ws.rs.POST"));
    static final List<DotName> DELETE = Arrays.asList(
            DotName.createSimple("javax.ws.rs.DELETE"),
            DotName.createSimple("jakarta.ws.rs.DELETE"));
    static final List<DotName> HEAD = Arrays.asList(
            DotName.createSimple("javax.ws.rs.HEAD"),
            DotName.createSimple("jakarta.ws.rs.HEAD"));
    static final List<DotName> OPTIONS = Arrays.asList(
            DotName.createSimple("javax.ws.rs.OPTIONS"),
            DotName.createSimple("jakarta.ws.rs.OPTIONS"));
    static final List<DotName> PATCH = Arrays.asList(
            DotName.createSimple("javax.ws.rs.PATCH"),
            DotName.createSimple("jakarta.ws.rs.PATCH"));

    static final String TO_RESPONSE_METHOD_NAME = "toResponse";

    private static final Set<DotName> methods = new LinkedHashSet<>();
    static {
        methods.addAll(GET);
        methods.addAll(PUT);
        methods.addAll(POST);
        methods.addAll(DELETE);
        methods.addAll(HEAD);
        methods.addAll(OPTIONS);
        methods.addAll(PATCH);
    }

    static final Set<DotName> HTTP_METHODS = Collections
            .unmodifiableSet(methods);

    private JaxRsConstants() {
    }

}
