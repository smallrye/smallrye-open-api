package io.smallrye.openapi.runtime.scanner.spi;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface ScannerSPILogging { // NOSONAR (use of constants in an interface)
    ScannerSPILogging log = Logger.getMessageLogger(ScannerSPILogging.class, ScannerSPILogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 7900, value = "Value '%s' is not a valid %s default")
    void invalidDefault(String segment, String primitive);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 7901, value = "Matrix parameter references missing path segment: %s")
    void missingPathSegment(String segment);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 7902, value = "@Parameter annotation found without %s parameter. Class: %s, Method: %s, Parameter.name: %s, Parameter.in: %s")
    void missingFrameworkParam(String frameworkName, String className, String method, String paramName, String paramIn);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 7903, value = "Duplicate operationId: %s produced by Class: %s, Method: %s and Class: %s, Method: %s")
    void duplicateOperationId(String operationId, String className, String method, String conflictingClassName,
            String conflictingMethod);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 7904, value = "Ignoring duplicate parameter named '%s' in location '%s' with style '%s', specified on %s")
    void duplicateParameter(String name, String location, String style, String target);

}
