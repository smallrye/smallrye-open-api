package io.smallrye.openapi.api;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface ApiLogging extends BasicLogger {

    ApiLogging logger = Logger.getMessageLogger(ApiLogging.class, ApiLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 500, value = "Adding model from %s...")
    void addingModel(String source);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 501, value = "Adding model %s from %s: %s")
    void addingModel(String name, String source, Object model);

}
