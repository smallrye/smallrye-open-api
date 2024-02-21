package io.smallrye.openapi.runtime.util;

import org.jboss.jandex.PrimitiveType;
import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SROAP", length = 5)
interface UtilMessages {
    UtilMessages msg = Messages.getBundle(UtilMessages.class);

    @Message(id = 8000, value = "ReferenceType must not be null")
    NullPointerException refTypeNotNull();

    @Message(id = 8001, value = "Unknown primitive: %s")
    IllegalArgumentException unknownPrimitive(PrimitiveType primitive);
}
