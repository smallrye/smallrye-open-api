package io.smallrye.openapi.runtime.reader;

import org.eclipse.microprofile.openapi.models.Components;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.models.ComponentsImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * Reading the Components annotation and json node
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#componentsObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ComponentsReader {
    private static final Logger LOG = Logger.getLogger(ComponentsReader.class);

    private ComponentsReader() {
    }

    /**
     * Reads any Components annotations.
     * 
     * @param context the scanning context
     * @param annotationValue the {@literal @}Components annotation
     * @return Components model
     */
    public static Components readComponents(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a @Components annotation.");
        AnnotationInstance nested = annotationValue.asNested();
        Components components = new ComponentsImpl();
        // TODO for EVERY item below, handle the case where the annotation is ref-only.  then strip the ref path and use the final segment as the name
        components.setCallbacks(
                CallbackReader.readCallbacks(context, nested.value(MPOpenApiConstants.COMPONENTS.PROP_CALLBACKS)));
        components.setExamples(ExampleReader.readExamples(nested.value(MPOpenApiConstants.COMPONENTS.PROP_EXAMPLES)));
        components.setHeaders(HeaderReader.readHeaders(context, nested.value(MPOpenApiConstants.COMPONENTS.PROP_HEADERS)));
        components.setLinks(LinkReader.readLinks(nested.value(MPOpenApiConstants.COMPONENTS.PROP_LINKS)));
        components.setParameters(
                ParameterReader.readParameters(context, nested.value(MPOpenApiConstants.COMPONENTS.PROP_PARAMETERS)));
        components.setRequestBodies(RequestBodyReader.readRequestBodies(context,
                nested.value(MPOpenApiConstants.COMPONENTS.PROP_REQUEST_BODIES)));
        components.setResponses(
                ResponseReader.readResponsesMap(context, nested.value(MPOpenApiConstants.COMPONENTS.PROP_RESPONSES)));
        components.setSchemas(SchemaReader.readSchemas(context, nested.value(MPOpenApiConstants.COMPONENTS.PROP_SCHEMAS)));
        components.setSecuritySchemes(
                SecuritySchemeReader.readSecuritySchemes(nested.value(MPOpenApiConstants.COMPONENTS.PROP_SECURITY_SCHEMES)));

        return components;
    }

    /**
     * Reads the {@link Components} OpenAPI nodes.
     * 
     * @param node the json node
     * @return Components model
     */
    public static Components readComponents(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a Components json node.");
        Components components = new ComponentsImpl();
        components.setCallbacks(CallbackReader.readCallbacks(node.get(MPOpenApiConstants.COMPONENTS.PROP_CALLBACKS)));
        components.setExamples(ExampleReader.readExamples(node.get(MPOpenApiConstants.COMPONENTS.PROP_EXAMPLES)));
        components.setHeaders(HeaderReader.readHeaders(node.get(MPOpenApiConstants.COMPONENTS.PROP_HEADERS)));
        components.setLinks(LinkReader.readLinks(node.get(MPOpenApiConstants.COMPONENTS.PROP_LINKS)));
        components.setParameters(ParameterReader.readParameters(node.get(MPOpenApiConstants.COMPONENTS.PROP_PARAMETERS)));
        components.setRequestBodies(
                RequestBodyReader.readRequestBodies(node.get(MPOpenApiConstants.COMPONENTS.PROP_REQUEST_BODIES)));
        components.setResponses(ResponseReader.readResponsesMap(node.get(MPOpenApiConstants.COMPONENTS.PROP_RESPONSES)));
        components.setSchemas(SchemaReader.readSchemas(node.get(MPOpenApiConstants.COMPONENTS.PROP_SCHEMAS)));
        components.setSecuritySchemes(
                SecuritySchemeReader.readSecuritySchemes(node.get(MPOpenApiConstants.COMPONENTS.PROP_SECURITY_SCHEMES)));
        ExtensionReader.readExtensions(node, components);
        return components;
    }
}
