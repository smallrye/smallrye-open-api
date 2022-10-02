package io.smallrye.openapi.runtime.scanner.spi;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SROAP", length = 5)
interface ScannerSPIMessages {
    ScannerSPIMessages msg = Messages.getBundle(ScannerSPIMessages.class);

    @Message(id = 7950, value = "Duplicate operationId: %s produced by Class: %s, Method: %s and Class: %s, Method: %s")
    IllegalStateException duplicateOperationId(String operationId, String className, String method, String conflictingClassName,
            String conflictingMethod);
}
