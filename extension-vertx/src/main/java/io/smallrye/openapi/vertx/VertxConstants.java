package io.smallrye.openapi.vertx;

import org.jboss.jandex.DotName;

/**
 * Constants related to Vertx
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class VertxConstants {

    static final DotName ROUTE = DotName.createSimple("io.quarkus.vertx.web.Route");
    static final DotName ROUTE_BASE = DotName.createSimple("io.quarkus.vertx.web.RouteBase");

    static final DotName REQUEST_BODY = DotName.createSimple("io.quarkus.vertx.web.Body");
    static final DotName PARAM = DotName.createSimple("io.quarkus.vertx.web.Param");
    static final DotName HEADER_PARAM = DotName.createSimple("io.quarkus.vertx.web.Header");

    private VertxConstants() {
    }
}
