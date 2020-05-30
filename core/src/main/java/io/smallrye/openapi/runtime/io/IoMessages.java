package io.smallrye.openapi.runtime.io;

import java.io.IOException;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SROAP", length = 5)
interface IoMessages {
    IoMessages msg = Messages.getBundle(IoMessages.class);

    @Message(id = 3000, value = "No file name for URL: %s")
    IOException noFileName(String url);

    @Message(id = 3001, value = "Invalid file name for URL: %s")
    IOException invalidFileName(String url);

    @Message(id = 3002, value = "Invalid file extension for URL (expected json, yaml, or yml): %s")
    IOException invalidFileExtension(String url);
}
