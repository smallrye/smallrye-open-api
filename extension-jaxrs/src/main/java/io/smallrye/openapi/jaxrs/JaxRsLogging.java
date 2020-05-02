package io.smallrye.openapi.jaxrs;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface JaxRsLogging {
    JaxRsLogging log = Logger.getMessageLogger(JaxRsLogging.class, JaxRsLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 10000, value = "Processing a JAX-RS resource class: %s")
    void processingClass(String className);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 10001, value = "Processing JaxRs method: %s")
    void processingMethod(String method);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10002, value = "Matrix parameter references missing path segment: %s")
    void missingPathSegment(String segment);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10003, value = "Value '%s' is not a valid %s default")
    void invalidDefault(String segment, String primitive);
}
