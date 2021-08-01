package io.smallrye.openapi.jaxrs;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.jandex.DotName;

/**
 * Constants related to JAX-RS
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class JaxRsConstants {

    static final Set<DotName> APPLICATION = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.core.Application"),
            DotName.createSimple("jakarta.ws.rs.core.Application")));
    static final Set<DotName> APPLICATION_PATH = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.ApplicationPath"),
            DotName.createSimple("jakarta.ws.rs.ApplicationPath")));
    static final Set<DotName> PATH = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.Path"),
            DotName.createSimple("jakarta.ws.rs.Path")));
    static final Set<DotName> PRODUCES = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.Produces"),
            DotName.createSimple("jakarta.ws.rs.Produces")));
    static final Set<DotName> CONSUMES = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.Consumes"),
            DotName.createSimple("jakarta.ws.rs.Consumes")));
    static final Set<DotName> EXCEPTION_MAPPER = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.ext.ExceptionMapper"),
            DotName.createSimple("jakarta.ws.rs.ext.ExceptionMapper")));
    static final Set<DotName> QUERY_PARAM = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.QueryParam"),
            DotName.createSimple("jakarta.ws.rs.QueryParam")));
    static final Set<DotName> FORM_PARAM = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.FormParam"),
            DotName.createSimple("jakarta.ws.rs.FormParam")));
    static final Set<DotName> COOKIE_PARAM = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.CookieParam"),
            DotName.createSimple("jakarta.ws.rs.CookieParam")));
    static final Set<DotName> PATH_PARAM = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.PathParam"),
            DotName.createSimple("jakarta.ws.rs.PathParam")));
    static final Set<DotName> HEADER_PARAM = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.HeaderParam"),
            DotName.createSimple("jakarta.ws.rs.HeaderParam")));
    static final Set<DotName> MATRIX_PARAM = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.MatrixParam"),
            DotName.createSimple("jakarta.ws.rs.MatrixParam")));
    static final Set<DotName> BEAN_PARAM = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.BeanParam"),
            DotName.createSimple("jakarta.ws.rs.BeanParam")));
    static final Set<DotName> ASYNC_RESPONSE = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.container.AsyncResponse"),
            DotName.createSimple("jakarta.ws.rs.container.AsyncResponse")));
    static final Set<DotName> DEFAULT_VALUE = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.DefaultValue"),
            DotName.createSimple("jakarta.ws.rs.DefaultValue")));
    static final Set<DotName> RESPONSE = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.core.Response"),
            DotName.createSimple("jakarta.ws.rs.core.Response")));
    static final Set<DotName> PATH_SEGMENT = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.core.PathSegment"),
            DotName.createSimple("jakarta.ws.rs.core.PathSegment")));

    static final DotName REGISTER_REST_CLIENT = DotName
            .createSimple("org.eclipse.microprofile.rest.client.inject.RegisterRestClient");

    static final Set<DotName> GET = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.GET"),
            DotName.createSimple("jakarta.ws.rs.GET")));
    static final Set<DotName> PUT = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.PUT"),
            DotName.createSimple("jakarta.ws.rs.PUT")));
    static final Set<DotName> POST = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.POST"),
            DotName.createSimple("jakarta.ws.rs.POST")));
    static final Set<DotName> DELETE = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.DELETE"),
            DotName.createSimple("jakarta.ws.rs.DELETE")));
    static final Set<DotName> HEAD = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.HEAD"),
            DotName.createSimple("jakarta.ws.rs.HEAD")));
    static final Set<DotName> OPTIONS = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.OPTIONS"),
            DotName.createSimple("jakarta.ws.rs.OPTIONS")));
    static final Set<DotName> PATCH = new TreeSet<>(Arrays.asList(
            DotName.createSimple("javax.ws.rs.PATCH"),
            DotName.createSimple("jakarta.ws.rs.PATCH")));

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

    public static final Set<DotName> HTTP_METHODS = Collections
            .unmodifiableSet(methods);

    private JaxRsConstants() {
    }

}
