package io.smallrye.openapi.model;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface ModelLogging extends BasicLogger {
    ModelLogging logger = Logger.getMessageLogger(MethodHandles.lookup(), ModelLogging.class,
            ModelLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10500, value = "Cyclic object reference detected in OpenAPI model, skipping current node")
    void cylicReferenceDetected();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10501, value = "Merge of property would result in cyclic object reference in OpenAPI model, skipping property '%s' in type %s")
    void cylicReferenceAvoided(String propertyName, String typeName);

}
