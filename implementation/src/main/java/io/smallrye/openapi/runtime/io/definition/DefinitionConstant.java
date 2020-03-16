package io.smallrye.openapi.runtime.io.definition;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.ExternalDocumentable;

/**
 * Constants related to Open API definition.
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#openapi-object
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class DefinitionConstant implements ExternalDocumentable {

    public static final DotName DOTNAME_OPEN_API_DEFINITION = DotName.createSimple(OpenAPIDefinition.class.getName());

    public static final String PROP_INFO = "info";
    public static final String PROP_SERVERS = "servers";
    public static final String PROP_PATHS = "paths";
    public static final String PROP_TAGS = "tags";
    public static final String PROP_COMPONENTS = "components";
    public static final String PROP_SECURITY = "security";
    public static final String PROP_OPENAPI = "openapi";

    private DefinitionConstant() {
    }
}
