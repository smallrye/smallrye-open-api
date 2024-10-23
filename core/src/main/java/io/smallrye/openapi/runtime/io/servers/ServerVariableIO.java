package io.smallrye.openapi.runtime.io.servers;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class ServerVariableIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<ServerVariable, V, A, O, AB, OB> {

    private static final String PROP_DEFAULT_VALUE = "defaultValue";
    private static final String PROP_DESCRIPTION = "description";
    // for annotations (reserved words in Java)
    private static final String PROP_ENUMERATION = "enumeration";

    public ServerVariableIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.SERVER_VARIABLE, Names.create(ServerVariable.class));
    }

    @Override
    public ServerVariable read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@ServerVariable");
        ServerVariable variable = OASFactory.createServerVariable();
        variable.setDescription(value(annotation, PROP_DESCRIPTION));
        String[] enumeration = value(annotation, PROP_ENUMERATION);
        if (enumeration != null) {
            variable.setEnumeration(new ArrayList<>(Arrays.asList(enumeration)));
        }
        variable.setDefaultValue(value(annotation, PROP_DEFAULT_VALUE));
        variable.setExtensions(extensionIO().readExtensible(annotation));
        return variable;
    }
}
