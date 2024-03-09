package io.smallrye.openapi.runtime.io.servers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.servers.ServerVariableImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class ServerVariableIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<ServerVariable, V, A, O, AB, OB> {

    private static final String PROP_ENUM = "enum";
    private static final String PROP_DEFAULT_VALUE = "defaultValue";
    private static final String PROP_DEFAULT = "default";
    private static final String PROP_DESCRIPTION = "description";
    // for annotations (reserved words in Java)
    private static final String PROP_ENUMERATION = "enumeration";

    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    protected ServerVariableIO(IOContext<V, A, O, AB, OB> context, ExtensionIO<V, A, O, AB, OB> extensionIO) {
        super(context, Names.SERVER_VARIABLE, Names.create(ServerVariable.class));
        this.extensionIO = extensionIO;
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

    public ServerVariable readObject(O node) {
        IoLogging.logger.singleJsonNode("ServerVariable");
        ServerVariable variable = new ServerVariableImpl();
        variable.setEnumeration(jsonIO().getArray(node, PROP_ENUM, jsonIO()::asString).orElse(null));
        variable.setDefaultValue(jsonIO().getString(node, PROP_DEFAULT));
        variable.setDescription(jsonIO().getString(node, PROP_DESCRIPTION));
        variable.setExtensions(extensionIO.readMap(node));
        return variable;
    }

    public Optional<O> write(ServerVariable model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_DEFAULT, jsonIO().toJson(model.getDefaultValue()));
            setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
            setIfPresent(node, PROP_ENUM, jsonIO().toJson(model.getEnumeration()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
