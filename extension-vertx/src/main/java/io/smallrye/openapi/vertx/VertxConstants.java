package io.smallrye.openapi.vertx;

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

    private VertxConstants() {
    }
}
