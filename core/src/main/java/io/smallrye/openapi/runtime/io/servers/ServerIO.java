package io.smallrye.openapi.runtime.io.servers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class ServerIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Server, V, A, O, AB, OB> {

    private static final String PROP_VARIABLES = "variables";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_URL = "url";

    public ServerIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.SERVER, Names.create(Server.class));
    }

    public List<Server> readList(AnnotationTarget target) {
        return readList(getRepeatableAnnotations(target));
    }

    public List<Server> readList(AnnotationValue annotations) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(this::readList)
                .orElse(null);
    }

    public List<Server> readList(AnnotationInstance[] annotations) {
        return readList(Arrays.asList(annotations));
    }

    public List<Server> readList(Collection<AnnotationInstance> annotations) {
        IoLogging.logger.annotationsArray("@Server");
        return annotations.stream()
                .map(this::read)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Server read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Server");
        Server server = OASFactory.createServer();
        server.setUrl(value(annotation, PROP_URL));
        server.setDescription(value(annotation, PROP_DESCRIPTION));
        server.setVariables(serverVariableIO().readMap(annotation.value(PROP_VARIABLES)));
        server.setExtensions(extensionIO().readExtensible(annotation));
        return server;
    }
}
