package io.smallrye.openapi.runtime.io;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.PathItem;
import org.eclipse.microprofile.openapi.annotations.PathItemOperation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callbacks;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.extensions.Extensions;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.links.Link;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
import org.eclipse.microprofile.openapi.annotations.media.Encoding;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBodySchema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.OAuthScope;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.servers.ServerVariable;
import org.eclipse.microprofile.openapi.annotations.servers.Servers;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.jandex.DotName;

public final class Names {

    private Names() {
    }

    public static DotName create(Class<?> clazz) {
        return DotName.createSimple(clazz);
    }

    public static DotName create(String className) {
        return DotName.createSimple(className);
    }

    private static final Map<DotName, DotName> repeatContainers = new HashMap<>();

    public static DotName containerOf(DotName repeatable) {
        return repeatContainers.get(repeatable);
    }

    public static final DotName OPENAPI_DEFINITION = create(OpenAPIDefinition.class);
    public static final DotName API_RESPONSE = create(APIResponse.class);
    public static final DotName API_RESPONSES = create(APIResponses.class);
    public static final DotName API_RESPONSE_SCHEMA = create(APIResponseSchema.class);
    public static final DotName CALLBACK = create(Callback.class);
    public static final DotName CALLBACKS = create(Callbacks.class);
    public static final DotName CALLBACK_OPERATION = create(CallbackOperation.class);
    public static final DotName COMPONENTS = create(Components.class);
    public static final DotName CONTACT = create(Contact.class);
    public static final DotName CONTENT = create(Content.class);
    public static final DotName DISCRIMINATOR_MAPPING = create(DiscriminatorMapping.class);
    public static final DotName ENCODING = create(Encoding.class);
    public static final DotName EXAMPLE_OBJECT = create(ExampleObject.class);
    public static final DotName EXTENSION = create(Extension.class);
    public static final DotName EXTENSIONS = create(Extensions.class);
    public static final DotName EXTERNAL_DOCUMENTATION = create(ExternalDocumentation.class);
    public static final DotName HEADER = create(Header.class);
    public static final DotName INFO = create(Info.class);
    public static final DotName LICENSE = create(License.class);
    public static final DotName LINK = create(Link.class);
    public static final DotName OAUTH_FLOW = create(OAuthFlow.class);
    public static final DotName OAUTH_FLOWS = create(OAuthFlows.class);
    public static final DotName OAUTH_SCOPE = create(OAuthScope.class);
    public static final DotName OPERATION = create(Operation.class);
    public static final DotName PARAMETER = create(Parameter.class);
    public static final DotName PARAMETERS = create(Parameters.class);
    public static final DotName PATH_ITEM = create(PathItem.class);
    public static final DotName PATH_ITEM_OPERATION = create(PathItemOperation.class);
    public static final DotName REQUEST_BODY = create(RequestBody.class);
    public static final DotName REQUEST_BODY_SCHEMA = create(RequestBodySchema.class);
    public static final DotName SCHEMA = create(Schema.class);
    public static final DotName SECURITY_REQUIREMENT = create(SecurityRequirement.class);
    public static final DotName SECURITY_REQUIREMENTS = create(SecurityRequirements.class);
    // Using strings for the following to allow running older versions of the TCK
    public static final DotName SECURITY_REQUIREMENTS_SET = create(
            "org.eclipse.microprofile.openapi.annotations.security.SecurityRequirementsSet");
    public static final DotName SECURITY_REQUIREMENTS_SETS = create(
            "org.eclipse.microprofile.openapi.annotations.security.SecurityRequirementsSets");
    public static final DotName SECURITY_SCHEME = create(SecurityScheme.class);
    public static final DotName SECURITY_SCHEMES = create(SecuritySchemes.class);
    public static final DotName SERVER = create(Server.class);
    public static final DotName SERVERS = create(Servers.class);
    public static final DotName SERVER_VARIABLE = create(ServerVariable.class);
    public static final DotName TAG = create(Tag.class);
    public static final DotName TAGS = create(Tags.class);

    static {
        repeatContainers.put(API_RESPONSE, API_RESPONSES);
        repeatContainers.put(CALLBACK, CALLBACKS);
        repeatContainers.put(EXTENSION, EXTENSIONS);
        repeatContainers.put(PARAMETER, PARAMETERS);
        repeatContainers.put(SECURITY_REQUIREMENT, SECURITY_REQUIREMENTS);
        repeatContainers.put(SECURITY_REQUIREMENTS_SET, SECURITY_REQUIREMENTS_SETS);
        repeatContainers.put(SECURITY_SCHEME, SECURITY_SCHEMES);
        repeatContainers.put(SERVER, SERVERS);
        repeatContainers.put(TAG, TAGS);
    }

}
