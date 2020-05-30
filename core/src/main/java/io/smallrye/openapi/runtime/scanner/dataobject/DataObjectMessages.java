package io.smallrye.openapi.runtime.scanner.dataobject;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SROAP", length = 5)
interface DataObjectMessages {
    DataObjectMessages msg = Messages.getBundle(DataObjectMessages.class);

    @Message(id = 7000, value = "Input parameter can not be null")
    RuntimeException notNull();
}
