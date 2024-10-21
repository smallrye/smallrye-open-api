package io.smallrye.openapi.runtime.io.links;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.model.ReferenceType;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;

public class LinkIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Link, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_OPERATION_ID = "operationId";
    private static final String PROP_PARAMETERS = "parameters";
    private static final String PROP_OPERATION_REF = "operationRef";
    private static final String PROP_SERVER = "server";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_REQUEST_BODY = "requestBody";

    public LinkIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.LINK, Names.create(Link.class));
    }

    @Override
    public Link read(AnnotationInstance annotationInstance) {
        IoLogging.logger.singleAnnotation("@Link");
        Link link = OASFactory.createLink();
        link.setOperationRef(value(annotationInstance, PROP_OPERATION_REF));
        link.setOperationId(value(annotationInstance, PROP_OPERATION_ID));
        link.setParameters(linkParameterIO().readMap(annotationInstance.value(PROP_PARAMETERS)));
        link.setDescription(value(annotationInstance, PROP_DESCRIPTION));
        link.setRequestBody(value(annotationInstance, PROP_REQUEST_BODY));
        link.setServer(serverIO().read(annotationInstance.value(PROP_SERVER)));
        link.setRef(ReferenceType.LINK.refValue(annotationInstance));
        link.setExtensions(extensionIO().readExtensible(annotationInstance));
        return link;
    }
}
