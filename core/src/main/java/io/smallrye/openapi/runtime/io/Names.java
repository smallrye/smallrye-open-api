package io.smallrye.openapi.runtime.io;

import java.util.Collection;
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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirementsSet;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirementsSets;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.servers.ServerVariable;
import org.eclipse.microprofile.openapi.annotations.servers.Servers;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.jandex.DotName;

public final class Names {

    private static final Map<DotName, DotName> repeatContainers = new HashMap<>();
    private static final Map<DotName, Class<?>> indexable = new HashMap<>();

    private Names() {
    }

    public static DotName create(Class<?> clazz) {
        return DotName.createSimple(clazz);
    }

    private static DotName createIndexable(Class<?> clazz) {
        DotName name = create(clazz);
        indexable.put(name, clazz);
        return name;
    }

    public static Collection<Class<?>> getIndexable() {
        return indexable.values();
    }

    public static DotName containerOf(DotName repeatable) {
        return repeatContainers.get(repeatable);
    }

    public static final DotName OPENAPI_DEFINITION = createIndexable(OpenAPIDefinition.class);
    public static final DotName API_RESPONSE = createIndexable(APIResponse.class);
    public static final DotName API_RESPONSES = createIndexable(APIResponses.class);
    public static final DotName API_RESPONSE_SCHEMA = createIndexable(APIResponseSchema.class);
    public static final DotName CALLBACK = createIndexable(Callback.class);
    public static final DotName CALLBACKS = createIndexable(Callbacks.class);
    public static final DotName CALLBACK_OPERATION = createIndexable(CallbackOperation.class);
    public static final DotName COMPONENTS = createIndexable(Components.class);
    public static final DotName CONTACT = createIndexable(Contact.class);
    public static final DotName CONTENT = createIndexable(Content.class);
    public static final DotName DISCRIMINATOR_MAPPING = createIndexable(DiscriminatorMapping.class);
    public static final DotName ENCODING = createIndexable(Encoding.class);
    public static final DotName EXAMPLE_OBJECT = createIndexable(ExampleObject.class);
    public static final DotName EXTENSION = createIndexable(Extension.class);
    public static final DotName EXTENSIONS = createIndexable(Extensions.class);
    public static final DotName EXTERNAL_DOCUMENTATION = createIndexable(ExternalDocumentation.class);
    public static final DotName HEADER = createIndexable(Header.class);
    public static final DotName INFO = createIndexable(Info.class);
    public static final DotName LICENSE = createIndexable(License.class);
    public static final DotName LINK = createIndexable(Link.class);
    public static final DotName OAUTH_FLOW = createIndexable(OAuthFlow.class);
    public static final DotName OAUTH_FLOWS = createIndexable(OAuthFlows.class);
    public static final DotName OAUTH_SCOPE = createIndexable(OAuthScope.class);
    public static final DotName OPERATION = createIndexable(Operation.class);
    public static final DotName PARAMETER = createIndexable(Parameter.class);
    public static final DotName PARAMETERS = createIndexable(Parameters.class);
    public static final DotName PATH_ITEM = createIndexable(PathItem.class);
    public static final DotName PATH_ITEM_OPERATION = createIndexable(PathItemOperation.class);
    public static final DotName REQUEST_BODY = createIndexable(RequestBody.class);
    public static final DotName REQUEST_BODY_SCHEMA = createIndexable(RequestBodySchema.class);
    public static final DotName SCHEMA = createIndexable(Schema.class);
    public static final DotName SECURITY_REQUIREMENT = createIndexable(SecurityRequirement.class);
    public static final DotName SECURITY_REQUIREMENTS = createIndexable(SecurityRequirements.class);
    // Using strings for the following to allow running older versions of the TCK
    public static final DotName SECURITY_REQUIREMENTS_SET = createIndexable(SecurityRequirementsSet.class);
    public static final DotName SECURITY_REQUIREMENTS_SETS = createIndexable(SecurityRequirementsSets.class);
    public static final DotName SECURITY_SCHEME = createIndexable(SecurityScheme.class);
    public static final DotName SECURITY_SCHEMES = createIndexable(SecuritySchemes.class);
    public static final DotName SERVER = createIndexable(Server.class);
    public static final DotName SERVERS = createIndexable(Servers.class);
    public static final DotName SERVER_VARIABLE = createIndexable(ServerVariable.class);
    public static final DotName TAG = createIndexable(Tag.class);
    public static final DotName TAGS = createIndexable(Tags.class);

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
