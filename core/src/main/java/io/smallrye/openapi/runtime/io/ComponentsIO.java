package io.smallrye.openapi.runtime.io;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Components;
import org.jboss.jandex.AnnotationInstance;

public class ComponentsIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Components, V, A, O, AB, OB> {

    private static final String PROP_CALLBACKS = "callbacks";
    private static final String PROP_LINKS = "links";
    private static final String PROP_SECURITY_SCHEMES = "securitySchemes";
    private static final String PROP_HEADERS = "headers";
    private static final String PROP_REQUEST_BODIES = "requestBodies";
    private static final String PROP_EXAMPLES = "examples";
    private static final String PROP_PARAMETERS = "parameters";
    private static final String PROP_RESPONSES = "responses";
    private static final String PROP_SCHEMAS = "schemas";
    private static final String PROP_PATH_ITEMS = "pathItems";

    public ComponentsIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.COMPONENTS, Names.create(Components.class));
    }

    @Override
    public Components read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Components");
        Components components = OASFactory.createComponents();
        components.setCallbacks(callbackIO().readMap(annotation.value(PROP_CALLBACKS)));
        components.setExamples(exampleObjectIO().readMap(annotation.value(PROP_EXAMPLES)));
        components.setHeaders(headerIO().readMap(annotation.value(PROP_HEADERS)));
        components.setLinks(linkIO().readMap(annotation.value(PROP_LINKS)));
        components.setParameters(parameterIO().readMap(annotation.value(PROP_PARAMETERS)));
        components.setPathItems(pathItemIO().readMap(annotation.value(PROP_PATH_ITEMS)));
        components.setRequestBodies(requestBodyIO().readMap(annotation.value(PROP_REQUEST_BODIES)));
        components.setResponses(apiResponseIO().readMap(annotation.value(PROP_RESPONSES)));
        components.setSchemas(schemaIO().readMap(annotation.value(PROP_SCHEMAS)));
        components.setSecuritySchemes(securitySchemeIO().readMap(annotation.value(PROP_SECURITY_SCHEMES)));
        components.setExtensions(extensionIO().readExtensible(annotation));

        return components;
    }

}
