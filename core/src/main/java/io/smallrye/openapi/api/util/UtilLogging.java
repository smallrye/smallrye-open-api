package io.smallrye.openapi.api.util;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface UtilLogging extends BasicLogger {
    UtilLogging logger = Logger.getMessageLogger(UtilLogging.class, UtilLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 1000, value = "Failed to introspect BeanInfo for: %s")
    void failedToIntrospectBeanInfo(Class<?> clazz, @Cause Throwable cause);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 1001, value = "Schema with zero references removed from #/components/schemas: %s")
    void unusedSchemaRemoved(String name);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1002, value = "Cyclic object reference detected in OpenAPI model, skipping current node")
    void cylicReferenceDetected();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1003, value = "Merge of property would result in cyclic object reference in OpenAPI model, skipping property '%s' in type %s")
    void cylicReferenceAvoided(String propertyName, String typeName);

}
