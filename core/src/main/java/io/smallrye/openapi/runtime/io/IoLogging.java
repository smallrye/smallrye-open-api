package io.smallrye.openapi.runtime.io;

import org.jboss.jandex.Type;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
public interface IoLogging extends BasicLogger {
    IoLogging log = Logger.getMessageLogger(IoLogging.class, IoLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2000, value = "Processing a map of %s annotations.")
    void annotationsMap(String annotation);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2001, value = "Processing a json map of %s nodes.")
    void jsonNodeMap(String node);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2002, value = "Processing a list of %s annotations.")
    void annotationsList(String annotation);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2003, value = "Processing a json list of %s.")
    void jsonList(String of);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2004, value = "Processing a single %s annotation.")
    void singleAnnotation(String annotation);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2005, value = "Processing an %s annotation.")
    void annotation(String annotation);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2006, value = "Processing a single %s annotation as a %s.")
    void singleAnnotationAs(String annotation, String as);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2007, value = "Processing a single %s json node.")
    void singleJsonNode(String node);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2008, value = "Processing an %s json node.")
    void jsonNode(String node);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2009, value = "Processing a single %s json object.")
    void singleJsonObject(String node);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2010, value = "Processing a json map of %s.")
    void jsonMap(String node);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 2011, value = "Error reading a CallbackOperation annotation.")
    void readingCallbackOperation(@Cause Throwable cause);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2012, value = "Processing a list of %s annotations into an %s.")
    void annotationsListInto(String annotation, String into);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2013, value = "Processing an enum %s")
    void enumProcessing(Type type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2014, value = "Processing an array of %s annotations.")
    void annotationsArray(String annotation);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2015, value = "Processing a json array of %s json nodes.")
    void jsonArray(String of);
}
