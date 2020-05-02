package io.smallrye.openapi.api;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SROAP", length = 5)
interface ApiMessages {
    ApiMessages msg = Messages.getBundle(ApiMessages.class);

    @Message(id = 0, value = "Model not initialized yet")
    IllegalStateException modelNotInitialized();

    @Message(id = 1, value = "Model already initialized")
    IllegalStateException modelAlreadyInitialized();

    @Message(id = 2, value = "OpenApiConfig must be set before init")
    IllegalStateException configMustBeSet();
}
