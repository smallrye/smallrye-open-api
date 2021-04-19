package io.smallrye.openapi.runtime.scanner.dataobject;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

import io.smallrye.openapi.runtime.OpenApiRuntimeException;

@MessageBundle(projectCode = "SROAP", length = 5)
interface DataObjectMessages {
    DataObjectMessages msg = Messages.getBundle(DataObjectMessages.class);

    @Message(id = 7000, value = "Input parameter can not be null")
    RuntimeException notNull();

    @Message(id = 7001, value = "Invalid property-naming-strategy: %s")
    OpenApiRuntimeException invalidPropertyNamingStrategy(String configValue);

    @Message(id = 7002, value = "Exception accessing property-naming-strategy: %s")
    OpenApiRuntimeException invalidPropertyNamingStrategyWithCause(String configValue, @Cause Throwable cause);

}
