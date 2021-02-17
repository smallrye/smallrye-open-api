package test.io.smallrye.openapi.tck;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface TckLogging {
    TckLogging log = Logger.getMessageLogger(TckLogging.class, TckLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 12000, value = "Indexing asset: %s from archive: %s")
    void indexing(String archivePath, String archive);
}
