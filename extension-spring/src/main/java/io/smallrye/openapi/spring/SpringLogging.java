package io.smallrye.openapi.spring;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface SpringLogging {
    SpringLogging log = Logger.getMessageLogger(SpringLogging.class, SpringLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 11000, value = "Ignoring %s annotation that is not on a class")
    void ignoringAnnotation(String className);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 11001, value = "Processing a Spring REST Controller class: %s")
    void processingController(String className);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 11002, value = "Processing Spring method: %s")
    void processingMethod(String methodName);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 11003, value = "Matrix parameter references missing path segment: %s")
    void missingPathSegment(String segment);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 11004, value = "Value '%s' is not a valid %s default")
    void invalidDefault(String segment, String primitive);

}
