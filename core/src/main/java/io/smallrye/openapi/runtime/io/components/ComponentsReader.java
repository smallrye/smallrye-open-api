package io.smallrye.openapi.runtime.io.components;

import org.eclipse.microprofile.openapi.models.Components;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.ComponentsImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.callback.CallbackReader;
import io.smallrye.openapi.runtime.io.example.ExampleReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.header.HeaderReader;
import io.smallrye.openapi.runtime.io.link.LinkReader;
import io.smallrye.openapi.runtime.io.parameter.ParameterReader;
import io.smallrye.openapi.runtime.io.requestbody.RequestBodyReader;
import io.smallrye.openapi.runtime.io.response.ResponseReader;
import io.smallrye.openapi.runtime.io.schema.SchemaReader;
import io.smallrye.openapi.runtime.io.securityscheme.SecuritySchemeReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * Reading the Components annotation and json node
 *
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#componentsObject">componentsObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ComponentsReader {

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
        IoLogging.logger.singleAnnotation("@Components");
        AnnotationInstance nested = annotationValue.asNested();
        Components components = new ComponentsImpl();
        // TODO for EVERY item below, handle the case where the annotation is ref-only.  then strip the ref path and use the final segment as the name
        components.setCallbacks(
                CallbackReader.readCallbacks(context, nested.value(ComponentsConstant.PROP_CALLBACKS)));
        components.setExamples(ExampleReader.readExamples(context, nested.value(ComponentsConstant.PROP_EXAMPLES)));
        components.setHeaders(HeaderReader.readHeaders(context, nested.value(ComponentsConstant.PROP_HEADERS)));
        components.setLinks(LinkReader.readLinks(context, nested.value(ComponentsConstant.PROP_LINKS)));
        components.setParameters(
                ParameterReader.readParameters(context, nested.value(ComponentsConstant.PROP_PARAMETERS)));
        components.setRequestBodies(RequestBodyReader.readRequestBodies(context,
                nested.value(ComponentsConstant.PROP_REQUEST_BODIES)));
        components.setResponses(
                ResponseReader.readResponsesMap(context, nested.value(ComponentsConstant.PROP_RESPONSES)));
        components.setSchemas(SchemaReader.readSchemas(context, nested.value(ComponentsConstant.PROP_SCHEMAS)));
        components.setSecuritySchemes(
                SecuritySchemeReader.readSecuritySchemes(context, nested.value(ComponentsConstant.PROP_SECURITY_SCHEMES)));
        components.setExtensions(ExtensionReader.readExtensions(context, nested));

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
        IoLogging.logger.singleJsonNode("Components");
        Components components = new ComponentsImpl();
        components.setCallbacks(CallbackReader.readCallbacks(node.get(ComponentsConstant.PROP_CALLBACKS)));
        components.setExamples(ExampleReader.readExamples(node.get(ComponentsConstant.PROP_EXAMPLES)));
        components.setHeaders(HeaderReader.readHeaders(node.get(ComponentsConstant.PROP_HEADERS)));
        components.setLinks(LinkReader.readLinks(node.get(ComponentsConstant.PROP_LINKS)));
        components.setParameters(ParameterReader.readParameters(node.get(ComponentsConstant.PROP_PARAMETERS)));
        components.setRequestBodies(
                RequestBodyReader.readRequestBodies(node.get(ComponentsConstant.PROP_REQUEST_BODIES)));
        components.setResponses(ResponseReader.readResponsesMap(node.get(ComponentsConstant.PROP_RESPONSES)));
        components.setSchemas(SchemaReader.readSchemas(node.get(ComponentsConstant.PROP_SCHEMAS)).orElse(null));
        components.setSecuritySchemes(
                SecuritySchemeReader.readSecuritySchemes(node.get(ComponentsConstant.PROP_SECURITY_SCHEMES)));
        ExtensionReader.readExtensions(node, components);
        return components;
    }
}
