package io.smallrye.openapi.api.constants;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callbacks;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.extensions.Extensions;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.servers.Servers;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.jandex.DotName;

/**
 * Constants related to MicroProfile OpenAPI.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class MPOpenApiConstants {

    public static final DotName OPEN_API_DEFINITION = DotName.createSimple(OpenAPIDefinition.class.getName());
    public static final DotName SECURITY_SCHEME = DotName.createSimple(SecurityScheme.class.getName());
    public static final DotName SECURITY_SCHEMES = DotName.createSimple(SecuritySchemes.class.getName());
    public static final DotName SECURITY_REQUIREMENT = DotName.createSimple(SecurityRequirement.class.getName());
    public static final DotName SECURITY_REQUIREMENTS = DotName.createSimple(SecurityRequirements.class.getName());
    public static final DotName CALLBACK = DotName.createSimple(Callback.class.getName());
    public static final DotName CALLBACKS = DotName.createSimple(Callbacks.class.getName());
    public static final DotName SCHEMA = DotName.createSimple(Schema.class.getName());
    public static final DotName TAG = DotName.createSimple(Tag.class.getName());
    public static final DotName TAGS = DotName.createSimple(Tags.class.getName());
    public static final DotName OPERATION = DotName.createSimple(Operation.class.getName());
    public static final DotName API_RESPONSE = DotName.createSimple(APIResponse.class.getName());
    public static final DotName API_RESPONSES = DotName.createSimple(APIResponses.class.getName());
    public static final DotName PARAMETER = DotName.createSimple(Parameter.class.getName());
    public static final DotName PARAMETERS = DotName.createSimple(Parameters.class.getName());
    public static final DotName REQUEST_BODY = DotName.createSimple(RequestBody.class.getName());
    public static final DotName SERVER = DotName.createSimple(Server.class.getName());
    public static final DotName SERVERS = DotName.createSimple(Servers.class.getName());
    public static final DotName EXTENSION = DotName.createSimple(Extension.class.getName());
    public static final DotName EXTENSIONS = DotName.createSimple(Extensions.class.getName());

    private MPOpenApiConstants() {
    }
}
