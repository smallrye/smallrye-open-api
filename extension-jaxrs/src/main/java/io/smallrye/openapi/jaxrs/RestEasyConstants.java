package io.smallrye.openapi.jaxrs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.DotName;

/**
 * Constants related to the RestEasy Project
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class RestEasyConstants {

    // RestEasy parameter extension annotations
    public static final DotName QUERY_PARAM = DotName
            .createSimple("org.jboss.resteasy.annotations.jaxrs.QueryParam");
    public static final DotName FORM_PARAM = DotName
            .createSimple("org.jboss.resteasy.annotations.jaxrs.FormParam");
    public static final DotName COOKIE_PARAM = DotName
            .createSimple("org.jboss.resteasy.annotations.jaxrs.CookieParam");
    public static final DotName PATH_PARAM = DotName
            .createSimple("org.jboss.resteasy.annotations.jaxrs.PathParam");
    public static final DotName HEADER_PARAM = DotName
            .createSimple("org.jboss.resteasy.annotations.jaxrs.HeaderParam");
    public static final DotName MATRIX_PARAM = DotName
            .createSimple("org.jboss.resteasy.annotations.jaxrs.MatrixParam");

    // RestEasy multi-part form annotations
    public static final DotName MULTIPART_FORM = DotName
            .createSimple("org.jboss.resteasy.annotations.providers.multipart.MultipartForm");
    public static final DotName PART_TYPE = DotName
            .createSimple("org.jboss.resteasy.annotations.providers.multipart.PartType");

    // RestEasy reactive parameter extension annotations
    public static final DotName REACTIVE_REST_QUERY = DotName
            .createSimple("org.jboss.resteasy.reactive.RestQuery");
    public static final DotName REACTIVE_REST_FORM = DotName
            .createSimple("org.jboss.resteasy.reactive.RestForm");
    public static final DotName REACTIVE_REST_COOKIE = DotName
            .createSimple("org.jboss.resteasy.reactive.RestCookie");
    public static final DotName REACTIVE_REST_PATH = DotName
            .createSimple("org.jboss.resteasy.reactive.RestPath");
    public static final DotName REACTIVE_REST_HEADER = DotName
            .createSimple("org.jboss.resteasy.reactive.RestHeader");
    public static final DotName REACTIVE_REST_MATRIX = DotName
            .createSimple("org.jboss.resteasy.reactive.RestMatrix");
    public static final DotName REACTIVE_REST_RESPONSE = DotName
            .createSimple("org.jboss.resteasy.reactive.RestResponse");

    // RestEasy reactive multi-part form annotations
    public static final DotName REACTIVE_MULTIPART_FORM = DotName
            .createSimple("org.jboss.resteasy.reactive.MultipartForm");
    public static final DotName REACTIVE_PART_TYPE = DotName
            .createSimple("org.jboss.resteasy.reactive.PartType");

    public static final Set<DotName> PART_TYPES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(PART_TYPE, REACTIVE_PART_TYPE)));

    // RestEasy multi-part request body types
    public static final DotName MULTIPART_INPUT = DotName
            .createSimple("org.jboss.resteasy.plugins.providers.multipart.MultipartInput");
    public static final DotName MULTIPART_FORM_DATA_INPUT = DotName
            .createSimple("org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput");
    public static final DotName MULTIPART_RELATED_INPUT = DotName
            .createSimple("org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedInput");

    public static final Set<DotName> MULTIPART_INPUTS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(MULTIPART_INPUT,
                    MULTIPART_FORM_DATA_INPUT,
                    MULTIPART_RELATED_INPUT)));

    // RestEasy multi-part response types
    public static final DotName MULTIPART_OUTPUT = DotName
            .createSimple("org.jboss.resteasy.plugins.providers.multipart.MultipartOutput");
    public static final DotName MULTIPART_FORM_DATA_OUTPUT = DotName
            .createSimple("org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput");
    public static final DotName MULTIPART_RELATED_OUTPUT = DotName
            .createSimple("org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedOutput");

    public static final Set<DotName> MULTIPART_OUTPUTS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(MULTIPART_OUTPUT,
                    MULTIPART_FORM_DATA_OUTPUT,
                    MULTIPART_RELATED_OUTPUT)));

    public static final DotName SERVER_EXCEPTION_MAPPER = DotName
            .createSimple("org.jboss.resteasy.reactive.server.ServerExceptionMapper");

    private RestEasyConstants() {
    }
}
