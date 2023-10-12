package io.smallrye.openapi.runtime.scanner;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface ScannerLogging extends BasicLogger {
    ScannerLogging logger = Logger.getMessageLogger(ScannerLogging.class, ScannerLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 4000, value = "Scanning deployment for %s Annotations.")
    void scanning(String annotationType);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 4001, value = "Starting processing with root: %s")
    void startProcessing(DotName root);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 4002, value = "Getting all fields for: %s in class: %s")
    void gettingFields(Type type, ClassInfo classInfo);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 4003, value = "Configured schema for %s could not be parsed")
    void errorParsingSchema(String className);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 4004, value = "Configured schema for %s has been registered")
    void configSchemaRegistered(String className);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 4005, value = "Could not find schema class in index: %s")
    void schemaTypeNotFound(DotName className);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 4006, value = "Configured schema type %s is not a valid type signature")
    void configSchemaTypeInvalid(String typeSignature, @Cause Throwable cause);

}
