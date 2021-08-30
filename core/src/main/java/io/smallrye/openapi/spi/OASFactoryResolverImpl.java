package io.smallrye.openapi.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.eclipse.microprofile.openapi.spi.OASFactoryResolver;

import io.smallrye.openapi.api.models.ComponentsImpl;
import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.api.models.PathsImpl;
import io.smallrye.openapi.api.models.callbacks.CallbackImpl;
import io.smallrye.openapi.api.models.examples.ExampleImpl;
import io.smallrye.openapi.api.models.headers.HeaderImpl;
import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.api.models.info.InfoImpl;
import io.smallrye.openapi.api.models.info.LicenseImpl;
import io.smallrye.openapi.api.models.links.LinkImpl;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.DiscriminatorImpl;
import io.smallrye.openapi.api.models.media.EncodingImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.media.XMLImpl;
import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.api.models.parameters.RequestBodyImpl;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import io.smallrye.openapi.api.models.responses.APIResponsesImpl;
import io.smallrye.openapi.api.models.security.OAuthFlowImpl;
import io.smallrye.openapi.api.models.security.OAuthFlowsImpl;
import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.api.models.security.SecuritySchemeImpl;
import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.api.models.servers.ServerVariableImpl;
import io.smallrye.openapi.api.models.tags.TagImpl;

/**
 * An implementation of the OpenAPI 1.0 spec's {@link OASFactoryResolver}. This class
 * is responsible for constructing vendor specific models given a {@link Constructible}
 * model interface.
 *
 * @author eric.wittmann@gmail.com
 */
public class OASFactoryResolverImpl extends OASFactoryResolver {

    private static final Map<Class<? extends Constructible>, Supplier<? extends Constructible>> registry = new HashMap<>();

    static <K extends Constructible, V extends K> void put(Class<K> key, Supplier<V> value) {
        registry.put(key, value);
    }

    static {
        put(APIResponse.class, APIResponseImpl::new);
        put(APIResponses.class, APIResponsesImpl::new);
        put(Callback.class, CallbackImpl::new);
        put(Components.class, ComponentsImpl::new);
        put(Contact.class, ContactImpl::new);
        put(Content.class, ContentImpl::new);
        put(Discriminator.class, DiscriminatorImpl::new);
        put(Encoding.class, EncodingImpl::new);
        put(Example.class, ExampleImpl::new);
        put(ExternalDocumentation.class, ExternalDocumentationImpl::new);
        put(Header.class, HeaderImpl::new);
        put(Info.class, InfoImpl::new);
        put(License.class, LicenseImpl::new);
        put(Link.class, LinkImpl::new);
        put(MediaType.class, MediaTypeImpl::new);
        put(OAuthFlow.class, OAuthFlowImpl::new);
        put(OAuthFlows.class, OAuthFlowsImpl::new);
        put(OpenAPI.class, OpenAPIImpl::new);
        put(Operation.class, OperationImpl::new);
        put(Parameter.class, ParameterImpl::new);
        put(PathItem.class, PathItemImpl::new);
        put(Paths.class, PathsImpl::new);
        put(RequestBody.class, RequestBodyImpl::new);
        put(Schema.class, SchemaImpl::new);
        put(SecurityRequirement.class, SecurityRequirementImpl::new);
        put(SecurityScheme.class, SecuritySchemeImpl::new);
        put(Server.class, ServerImpl::new);
        put(ServerVariable.class, ServerVariableImpl::new);
        put(Tag.class, TagImpl::new);
        put(XML.class, XMLImpl::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.spi.OASFactoryResolver#createObject(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Constructible> T createObject(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return (T) registry.getOrDefault(clazz, () -> this.unknownType(clazz)).get();
    }

    <T extends Constructible> T unknownType(Class<T> clazz) {
        throw SpiMessages.msg.classNotConstructible(clazz.getName());
    }
}
