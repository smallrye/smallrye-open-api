package io.smallrye.openapi.runtime.io.servers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.servers.ServerVariableImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class ServerVariableIO extends MapModelIO<ServerVariable> {

    private static final String PROP_ENUM = "enum";
    private static final String PROP_DEFAULT_VALUE = "defaultValue";
    private static final String PROP_DEFAULT = "default";
    private static final String PROP_DESCRIPTION = "description";
    // for annotations (reserved words in Java)
    private static final String PROP_ENUMERATION = "enumeration";

    private final ExtensionIO extensionIO;

    protected ServerVariableIO(AnnotationScannerContext context) {
        super(context, Names.SERVER_VARIABLE, Names.create(ServerVariable.class));
        extensionIO = new ExtensionIO(context);
    }

    @Override
    public ServerVariable read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@ServerVariable");
        ServerVariable variable = new ServerVariableImpl();
        variable.setDescription(value(annotation, PROP_DESCRIPTION));
        String[] enumeration = value(annotation, PROP_ENUMERATION);
        if (enumeration != null) {
            variable.setEnumeration(new ArrayList<>(Arrays.asList(enumeration)));
        }
        variable.setDefaultValue(value(annotation, PROP_DEFAULT_VALUE));
        variable.setExtensions(extensionIO.readExtensible(annotation));
        return variable;
    }

    public ServerVariable read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("ServerVariable");
        ServerVariable variable = new ServerVariableImpl();
        JsonNode enumNode = node.get(PROP_ENUM);
        if (enumNode != null && enumNode.isArray()) {
            List<String> enums = new ArrayList<>(enumNode.size());
            for (JsonNode n : enumNode) {
                enums.add(n.asText());
            }
            variable.setEnumeration(enums);
        }
        variable.setDefaultValue(JsonUtil.stringProperty(node, PROP_DEFAULT));
        variable.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        extensionIO.readMap(node).forEach(variable::addExtension);
        return variable;
    }

    public Optional<ObjectNode> write(ServerVariable model) {
        return optionalJsonObject(model).map(node -> {
            JsonUtil.stringProperty(node, PROP_DEFAULT, model.getDefaultValue());
            JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
            Optional.ofNullable(model.getEnumeration())
                    .ifPresent(enumeration -> enumeration.forEach(node.putArray(PROP_ENUM)::add));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }
}
