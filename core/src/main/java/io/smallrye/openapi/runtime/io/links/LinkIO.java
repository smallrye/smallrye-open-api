package io.smallrye.openapi.runtime.io.links;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.links.Link;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.links.LinkImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.servers.ServerIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class LinkIO extends MapModelIO<Link> implements ReferenceIO {

    private static final String PROP_OPERATION_ID = "operationId";
    private static final String PROP_PARAMETERS = "parameters";
    private static final String PROP_OPERATION_REF = "operationRef";
    private static final String PROP_SERVER = "server";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_REQUEST_BODY = "requestBody";

    private final ServerIO serverIO;
    private final LinkParameterIO linkParameterIO;
    private final ExtensionIO extensionIO;

    public LinkIO(AnnotationScannerContext context) {
        super(context, Names.LINK, Names.create(Link.class));
        serverIO = new ServerIO(context);
        linkParameterIO = new LinkParameterIO(context);
        extensionIO = new ExtensionIO(context);
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
    public Link read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Link");
        Link link = new LinkImpl();
        link.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));

        link.setOperationRef(JsonUtil.stringProperty(node, PROP_OPERATION_REF));
        link.setOperationId(JsonUtil.stringProperty(node, PROP_OPERATION_ID));
        link.setParameters(linkParameterIO.readMap(node.get(PROP_PARAMETERS)));
        link.setRequestBody(JsonUtil.readObject(node.get(PROP_REQUEST_BODY)));
        link.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        link.setServer(serverIO.read(node.get(PROP_SERVER)));
        extensionIO.readMap(node).forEach(link::addExtension);
        return link;
    }

    public Optional<ObjectNode> write(Link model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
            } else {
                JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPERATION_REF, model.getOperationRef());
                JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPERATION_ID, model.getOperationId());
                setIfPresent(node, PROP_PARAMETERS, linkParameterIO.write(model.getParameters()));
                ObjectWriter.writeObject(node, PROP_REQUEST_BODY, model.getRequestBody());
                JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
                setIfPresent(node, PROP_SERVER, serverIO.write(model.getServer()));
                setAllIfPresent(node, extensionIO.write(model));
            }

            return node;
        });
    }
}
