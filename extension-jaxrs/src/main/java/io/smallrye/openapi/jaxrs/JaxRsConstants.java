package io.smallrye.openapi.jaxrs;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.PathItem.HttpMethod;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
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
    static final Set<DotName> CONTEXTS = Stream.of("javax", "jakarta")
            .map(prefix -> DotName.createComponentized(null, prefix))
            .map(prefix -> DotName.createComponentized(prefix, "ws"))
            .map(prefix -> DotName.createComponentized(prefix, "rs"))
            .map(prefix -> DotName.createComponentized(prefix, "core"))
            .flatMap(prefix -> Stream.of("Application", "UriInfo", "Request", "HttpHeaders", "SecurityContext")
                    .map(simpleName -> DotName.createComponentized(prefix, simpleName)))
            .collect(Collectors.toSet());

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

    private static final AnnotationValue[] EMPTY_VALUES = new AnnotationValue[0];
    public static final Set<AnnotationInstance> HTTP_METHOD_INSTANCES = methods
            .stream()
            .map(name -> AnnotationInstance.create(name, null, EMPTY_VALUES))
            .collect(Collectors.toUnmodifiableSet());

    public static final Map<PathItem.HttpMethod, Set<DotName>> HTTP_METHOD_ANNOTATIONS;
    static {
        Map<PathItem.HttpMethod, Set<DotName>> annotations = new EnumMap<>(PathItem.HttpMethod.class);
        annotations.put(HttpMethod.DELETE, DELETE);
        annotations.put(HttpMethod.GET, GET);
        annotations.put(HttpMethod.HEAD, HEAD);
        annotations.put(HttpMethod.OPTIONS, OPTIONS);
        annotations.put(HttpMethod.PATCH, PATCH);
        annotations.put(HttpMethod.POST, POST);
        annotations.put(HttpMethod.PUT, PUT);
        HTTP_METHOD_ANNOTATIONS = Collections.unmodifiableMap(annotations);
    }

    private JaxRsConstants() {
    }

}
