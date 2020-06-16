package io.smallrye.openapi.runtime.scanner.dataobject;

import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROAP", length = 5)
interface DataObjectLogging extends BasicLogger {
    DataObjectLogging log = Logger.getMessageLogger(DataObjectLogging.class, DataObjectLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6000, value = "Processing @Schema annotation %s on a field %s")
    void processingFieldAnnotation(AnnotationInstance annotation, String propertyKey);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6001, value = "Annotation value has invalid format: %s")
    void invalidAnnotationFormat(String decimalValue);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6002, value = "Possible cycle was detected at: %s. Will not search further.")
    void possibleCycle(ClassInfo classInfo);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6003, value = "Adding child node to path: %s")
    void addingChildNode(ClassInfo classInfo);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6004, value = "Path: %s")
    void path(String entry);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6005, value = "Ignoring type that is member of ignore set: %s")
    void ignoringType(DotName classInfo);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6006, value = "Ignoring type and adding to ignore set: %s")
    void ignoringTypeAndAddingToSet(DotName classInfo);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6007, value = "Processing an array %s")
    void processingArray(Type type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6008, value = "Processing parameterized type %s")
    void processingParametrizedType(ParameterizedType type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 6009, value = "Processing %s. Will treat as an %s.")
    void processingTypeAs(String type, String asType);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 31010, value = "Processing an enum %s")
    void processingEnum(Type type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 31011, value = "Encountered type not in Jandex index that is not well-known type. Will not traverse it: %s")
    void typeNotInJandexIndex(Type type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 31012, value = "Resolved type %s -> %s")
    void resolvedType(Type type, Type resolvedType);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 31013, value = "Is a terminal type %s")
    void terminalType(Type type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 31014, value = "Attempting to do TYPE_VARIABLE substitution: %s -> %s")
    void typeVarSubstitution(Type type, Type resolvedType);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 31015, value = "Class for type %s not available")
    void classNotAvailable(Type type);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 31016, value = "Unanticipated mismatch between type arguments and type variables \n" +
            "Args: %s\n Vars:%s")
    void classNotAvailable(List<TypeVariable> typeVariables, List<Type> arguments);
}
