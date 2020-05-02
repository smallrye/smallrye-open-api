package io.smallrye.openapi.runtime.scanner;

import java.util.NoSuchElementException;

import org.jboss.jandex.DotName;
import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SROAP", length = 5)
interface ScannerMessages {
    ScannerMessages msg = Messages.getBundle(ScannerMessages.class);

    @Message(id = 5000, value = "Failed to create instance of custom schema registry: %s")
    RuntimeException failedCreateInstance(String schemaRegistry, @Cause Throwable throwable);

    @Message(id = 5001, value = "Class schema not registered: %s")
    NoSuchElementException notRegistered(DotName schema);
}
