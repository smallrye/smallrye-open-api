package io.smallrye.openapi.spi;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SROAP", length = 5)
interface SpiMessages {
    SpiMessages msg = Messages.getBundle(SpiMessages.class);

    @Message(id = 9000, value = "Class '%s' is not Constructible.")
    IllegalArgumentException classNotConstructible(String className);
}
