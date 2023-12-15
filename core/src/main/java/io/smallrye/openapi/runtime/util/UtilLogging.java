package io.smallrye.openapi.runtime.util;

import org.jboss.jandex.DotName;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface UtilLogging extends BasicLogger {
    UtilLogging logger = Logger.getMessageLogger(UtilLogging.class, UtilLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 8500, value = "Fall back to reflection: %s instanceof %s")
    void reflectionInstanceOf(Class<?> subject, Class<?> object);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 8501, value = "Search annotation %s for %s")
    void composedAnnotationSearch(DotName seekName, DotName annotationName);

}
