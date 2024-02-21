package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.Components;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.ComponentsImpl;
import io.smallrye.openapi.runtime.io.callbacks.CallbackIO;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.headers.HeaderIO;
import io.smallrye.openapi.runtime.io.links.LinkIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.media.ExampleObjectIO;
import io.smallrye.openapi.runtime.io.media.SchemaIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.parameters.RequestBodyIO;
import io.smallrye.openapi.runtime.io.responses.APIResponseIO;
import io.smallrye.openapi.runtime.io.security.SecuritySchemeIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class ComponentsIO extends ModelIO<Components> {

    private static final String PROP_CALLBACKS = "callbacks";
    private static final String PROP_LINKS = "links";
    private static final String PROP_SECURITY_SCHEMES = "securitySchemes";
    private static final String PROP_HEADERS = "headers";
    private static final String PROP_REQUEST_BODIES = "requestBodies";
    private static final String PROP_EXAMPLES = "examples";
    private static final String PROP_PARAMETERS = "parameters";
    private static final String PROP_RESPONSES = "responses";
    private static final String PROP_SCHEMAS = "schemas";

    private final APIResponseIO responseIO;
    private final HeaderIO headerIO;
    private final SchemaIO schemaIO;
    private final ExampleObjectIO exampleObjectIO;
    private final CallbackIO callbackIO;
    private final LinkIO linkIO;
    private final ParameterIO parameterIO;
    private final RequestBodyIO requestBodyIO;
    private final SecuritySchemeIO securitySchemeIO;
    private final ExtensionIO extensionIO;

    public ComponentsIO(AnnotationScannerContext context, ContentIO contentIO) {
        super(context, Names.COMPONENTS, Names.create(Components.class));
        responseIO = new APIResponseIO(context, contentIO);
        headerIO = new HeaderIO(context, contentIO);
        schemaIO = new SchemaIO(context);
        exampleObjectIO = new ExampleObjectIO(context);
        callbackIO = new CallbackIO(context, contentIO);
        linkIO = new LinkIO(context);
        parameterIO = new ParameterIO(context, contentIO);
        requestBodyIO = new RequestBodyIO(context, contentIO);
        securitySchemeIO = new SecuritySchemeIO(context);
        extensionIO = new ExtensionIO(context);
    }

    public CallbackIO callbacks() {
        return callbackIO;
    }

    public SecuritySchemeIO securitySchemes() {
        return securitySchemeIO;
    }

    @Override
    public Components read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Components");
        Components components = new ComponentsImpl();
        components.setCallbacks(callbackIO.readMap(annotation.value(PROP_CALLBACKS)));
        components.setExamples(exampleObjectIO.readMap(annotation.value(PROP_EXAMPLES)));
        components.setHeaders(headerIO.readMap(annotation.value(PROP_HEADERS)));
        components.setLinks(linkIO.readMap(annotation.value(PROP_LINKS)));
        components.setParameters(parameterIO.readMap(annotation.value(PROP_PARAMETERS)));
        components.setRequestBodies(requestBodyIO.readMap(annotation.value(PROP_REQUEST_BODIES)));
        components.setResponses(responseIO.readMap(annotation.value(PROP_RESPONSES)));
        components.setSchemas(schemaIO.readMap(annotation.value(PROP_SCHEMAS)));
        components.setSecuritySchemes(securitySchemeIO.readMap(annotation.value(PROP_SECURITY_SCHEMES)));
        components.setExtensions(extensionIO.readExtensible(annotation));

        return components;
    }

    @Override
    public Components read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Components");
        Components components = new ComponentsImpl();
        components.setCallbacks(callbackIO.readMap(node.get(PROP_CALLBACKS)));
        components.setExamples(exampleObjectIO.readMap(node.get(PROP_EXAMPLES)));
        components.setHeaders(headerIO.readMap(node.get(PROP_HEADERS)));
        components.setLinks(linkIO.readMap(node.get(PROP_LINKS)));
        components.setParameters(parameterIO.readMap(node.get(PROP_PARAMETERS)));
        components.setRequestBodies(requestBodyIO.readMap(node.get(PROP_REQUEST_BODIES)));
        components.setResponses(responseIO.readMap(node.get(PROP_RESPONSES)));
        components.setSchemas(schemaIO.readMap(node.get(PROP_SCHEMAS)));
        components.setSecuritySchemes(securitySchemeIO.readMap(node.get(PROP_SECURITY_SCHEMES)));
        extensionIO.readMap(node).forEach(components::addExtension);
        return components;
    }

    public Optional<ObjectNode> write(Components model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_SCHEMAS, schemaIO.write(model.getSchemas()));
            setIfPresent(node, PROP_RESPONSES, responseIO.write(model.getResponses()));
            setIfPresent(node, PROP_PARAMETERS, parameterIO.write(model.getParameters()));
            setIfPresent(node, PROP_EXAMPLES, exampleObjectIO.write(model.getExamples()));
            setIfPresent(node, PROP_REQUEST_BODIES, requestBodyIO.write(model.getRequestBodies()));
            setIfPresent(node, PROP_HEADERS, headerIO.write(model.getHeaders()));
            setIfPresent(node, PROP_SECURITY_SCHEMES, securitySchemeIO.write(model.getSecuritySchemes()));
            setIfPresent(node, PROP_LINKS, linkIO.write(model.getLinks()));
            setIfPresent(node, PROP_CALLBACKS, callbackIO.write(model.getCallbacks()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }
}
