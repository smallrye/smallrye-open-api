package io.smallrye.openapi.vertx;

import static java.lang.invoke.MethodHandles.lookup;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface VertxLogging {
    VertxLogging log = Logger.getMessageLogger(lookup(), VertxLogging.class, VertxLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 14000, value = "Ignoring %s annotation that is not on a method")
    void ignoringAnnotation(String className);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 14001, value = "Processing class with Vert.x routes: %s")
    void processingRouteClass(String className);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 14002, value = "Processing Vert.x method: %s")
    void processingMethod(String methodName);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 14003, value = "Matrix parameter references missing path segment: %s")
    void missingPathSegment(String segment);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 14004, value = "Value '%s' is not a valid %s default")
    void invalidDefault(String segment, String primitive);

}
