package io.smallrye.openapi.vertx;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.jandex.DotName;

/**
 * Constants related to Vertx
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class VertxConstants {

    public static final DotName ROUTE = DotName.createSimple("io.quarkus.vertx.web.Route");
    public static final DotName ROUTE_BASE = DotName.createSimple("io.quarkus.vertx.web.RouteBase");

    public static final DotName REQUEST_BODY = DotName.createSimple("io.quarkus.vertx.web.Body");
    public static final DotName PARAM = DotName.createSimple("io.quarkus.vertx.web.Param");
    public static final DotName HEADER_PARAM = DotName.createSimple("io.quarkus.vertx.web.Header");

    public static final DotName ROUTING_CONTEXT = DotName.createSimple("io.vertx.ext.web.RoutingContext");
    public static final DotName R_ROUTING_CONTEXT = DotName.createSimple("io.vertx.reactivex.ext.web.RoutingContext");
    public static final DotName ROUTING_EXCHANGE = DotName.createSimple("io.quarkus.vertx.web.RoutingExchange");
    public static final DotName HTTP_SERVER_REQUEST = DotName.createSimple("io.vertx.core.http.HttpServerRequest");
    public static final DotName HTTP_SERVER_RESPONSE = DotName.createSimple("io.vertx.core.http.HttpServerResponse");
    public static final DotName R_HTTP_SERVER_REQUEST = DotName.createSimple("io.vertx.reactivex.core.http.HttpServerRequest");
    public static final DotName R_HTTP_SERVER_RESPONSE = DotName
            .createSimple("io.vertx.reactivex.core.http.HttpServerResponse");

    public static final List<DotName> INTERNAL_PARAMETERS = Collections.unmodifiableList(Arrays.asList(ROUTING_CONTEXT,
            R_ROUTING_CONTEXT,
            ROUTING_EXCHANGE,
            HTTP_SERVER_REQUEST,
            HTTP_SERVER_RESPONSE,
            R_HTTP_SERVER_REQUEST,
            R_HTTP_SERVER_RESPONSE));

    private VertxConstants() {
    }
}
