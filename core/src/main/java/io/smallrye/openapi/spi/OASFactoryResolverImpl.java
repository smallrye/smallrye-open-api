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

    private static class OASFactoryResolverRegistry {

        private static final Map<Class<? extends Constructible>, Supplier<? extends Constructible>> REGISTRY;

        static {
            Map<Class<? extends Constructible>, Supplier<? extends Constructible>> registry = new HashMap<>(30);
            registry.put(APIResponse.class, APIResponseImpl::new);
            registry.put(APIResponses.class, APIResponsesImpl::new);
            registry.put(Callback.class, CallbackImpl::new);
            registry.put(Components.class, ComponentsImpl::new);
            registry.put(Contact.class, ContactImpl::new);
            registry.put(Content.class, ContentImpl::new);
            registry.put(Discriminator.class, DiscriminatorImpl::new);
            registry.put(Encoding.class, EncodingImpl::new);
            registry.put(Example.class, ExampleImpl::new);
            registry.put(ExternalDocumentation.class, ExternalDocumentationImpl::new);
            registry.put(Header.class, HeaderImpl::new);
            registry.put(Info.class, InfoImpl::new);
            registry.put(License.class, LicenseImpl::new);
            registry.put(Link.class, LinkImpl::new);
            registry.put(MediaType.class, MediaTypeImpl::new);
            registry.put(OAuthFlow.class, OAuthFlowImpl::new);
            registry.put(OAuthFlows.class, OAuthFlowsImpl::new);
            registry.put(OpenAPI.class, OpenAPIImpl::new);
            registry.put(Operation.class, OperationImpl::new);
            registry.put(Parameter.class, ParameterImpl::new);
            registry.put(PathItem.class, PathItemImpl::new);
            registry.put(Paths.class, PathsImpl::new);
            registry.put(RequestBody.class, RequestBodyImpl::new);
            registry.put(Schema.class, SchemaImpl::new);
            registry.put(SecurityRequirement.class, SecurityRequirementImpl::new);
            registry.put(SecurityScheme.class, SecuritySchemeImpl::new);
            registry.put(Server.class, ServerImpl::new);
            registry.put(ServerVariable.class, ServerVariableImpl::new);
            registry.put(Tag.class, TagImpl::new);
            registry.put(XML.class, XMLImpl::new);
            REGISTRY = registry;
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.spi.OASFactoryResolver#createObject(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Constructible> T createObject(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return (T) OASFactoryResolverRegistry.REGISTRY.getOrDefault(clazz, () -> this.unknownType(clazz)).get();
    }

    <T extends Constructible> T unknownType(Class<T> clazz) {
        throw SpiMessages.msg.classNotConstructible(clazz.getName());
    }
}
