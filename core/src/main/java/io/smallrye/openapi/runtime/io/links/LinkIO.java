package io.smallrye.openapi.runtime.io.links;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.links.Link;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.links.LinkImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.servers.ServerIO;

public class LinkIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Link, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_OPERATION_ID = "operationId";
    private static final String PROP_PARAMETERS = "parameters";
    private static final String PROP_OPERATION_REF = "operationRef";
    private static final String PROP_SERVER = "server";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_REQUEST_BODY = "requestBody";

    private final ServerIO<V, A, O, AB, OB> serverIO;
    private final LinkParameterIO<V, A, O, AB, OB> linkParameterIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public LinkIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.LINK, Names.create(Link.class));
        serverIO = new ServerIO<>(context);
        linkParameterIO = new LinkParameterIO<>(context);
        extensionIO = new ExtensionIO<>(context);
    }

    @Override
    public Link read(AnnotationInstance annotationInstance) {
        IoLogging.logger.singleAnnotation("@Link");
        Link link = new LinkImpl();
        link.setOperationRef(value(annotationInstance, PROP_OPERATION_REF));
        link.setOperationId(value(annotationInstance, PROP_OPERATION_ID));
        link.setParameters(linkParameterIO.readMap(annotationInstance.value(PROP_PARAMETERS)));
        link.setDescription(value(annotationInstance, PROP_DESCRIPTION));
        link.setRequestBody(value(annotationInstance, PROP_REQUEST_BODY));
        link.setServer(serverIO.read(annotationInstance.value(PROP_SERVER)));
        link.setRef(ReferenceType.LINK.refValue(annotationInstance));
        link.setExtensions(extensionIO.readExtensible(annotationInstance));
        return link;
    }

    @Override
    public Link readObject(O node) {
        IoLogging.logger.singleJsonNode("Link");
        Link link = new LinkImpl();
        link.setRef(readReference(node));
        link.setOperationRef(jsonIO().getString(node, PROP_OPERATION_REF));
        link.setOperationId(jsonIO().getString(node, PROP_OPERATION_ID));
        link.setParameters(linkParameterIO.readMap(jsonIO().getValue(node, PROP_PARAMETERS)));
        link.setRequestBody(jsonIO().fromJson(jsonIO().getValue(node, PROP_REQUEST_BODY)));
        link.setDescription(jsonIO().getString(node, PROP_DESCRIPTION));
        link.setServer(serverIO.readValue(jsonIO().getValue(node, PROP_SERVER)));
        extensionIO.readMap(node).forEach(link::addExtension);
        return link;
    }

    public Optional<O> write(Link model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                setReference(node, model);
            } else {
                setIfPresent(node, PROP_OPERATION_REF, jsonIO().toJson(model.getOperationRef()));
                setIfPresent(node, PROP_OPERATION_ID, jsonIO().toJson(model.getOperationId()));
                setIfPresent(node, PROP_PARAMETERS, linkParameterIO.write(model.getParameters()));
                setIfPresent(node, PROP_REQUEST_BODY, jsonIO().toJson(model.getRequestBody()));
                setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
                setIfPresent(node, PROP_SERVER, serverIO.write(model.getServer()));
                setAllIfPresent(node, extensionIO.write(model));
            }

            return node;
        }).map(jsonIO()::buildObject);
    }
}
