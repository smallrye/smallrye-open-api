package test.io.smallrye.openapi.tck;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SROAP", length = 5)
interface ExtraSuiteMessages {
    ExtraSuiteMessages msg = Messages.getBundle(ExtraSuiteMessages.class);

    @Message(id = 13000, value = "Archive was null!")
    RuntimeException nullArchive();

    @Message(id = 13001, value = "No @Deployment archive found for test.")
    Exception missingDeploymentArchive();
}
