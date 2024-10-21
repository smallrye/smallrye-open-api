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
            registry.put(APIResponse.class, io.smallrye.openapi.internal.models.responses.APIResponse::new);
            registry.put(APIResponses.class, io.smallrye.openapi.internal.models.responses.APIResponses::new);
            registry.put(Callback.class, io.smallrye.openapi.internal.models.callbacks.Callback::new);
            registry.put(Components.class, io.smallrye.openapi.internal.models.Components::new);
            registry.put(Contact.class, io.smallrye.openapi.internal.models.info.Contact::new);
            registry.put(Content.class, io.smallrye.openapi.internal.models.media.Content::new);
            registry.put(Discriminator.class, io.smallrye.openapi.internal.models.media.Discriminator::new);
            registry.put(Encoding.class, io.smallrye.openapi.internal.models.media.Encoding::new);
            registry.put(Example.class, io.smallrye.openapi.internal.models.examples.Example::new);
            registry.put(ExternalDocumentation.class, io.smallrye.openapi.internal.models.ExternalDocumentation::new);
            registry.put(Header.class, io.smallrye.openapi.internal.models.headers.Header::new);
            registry.put(Info.class, io.smallrye.openapi.internal.models.info.Info::new);
            registry.put(License.class, io.smallrye.openapi.internal.models.info.License::new);
            registry.put(Link.class, io.smallrye.openapi.internal.models.links.Link::new);
            registry.put(MediaType.class, io.smallrye.openapi.internal.models.media.MediaType::new);
            registry.put(OAuthFlow.class, io.smallrye.openapi.internal.models.security.OAuthFlow::new);
            registry.put(OAuthFlows.class, io.smallrye.openapi.internal.models.security.OAuthFlows::new);
            registry.put(OpenAPI.class, io.smallrye.openapi.internal.models.OpenAPI::new);
            // Using deprecated model until Quarkus no longer references it directly
            registry.put(Operation.class, io.smallrye.openapi.api.models.OperationImpl::new);
            registry.put(Parameter.class, io.smallrye.openapi.internal.models.parameters.Parameter::new);
            registry.put(PathItem.class, io.smallrye.openapi.internal.models.PathItem::new);
            registry.put(Paths.class, io.smallrye.openapi.internal.models.Paths::new);
            registry.put(RequestBody.class, io.smallrye.openapi.internal.models.parameters.RequestBody::new);
            registry.put(Schema.class, io.smallrye.openapi.internal.models.media.Schema::new);
            registry.put(SecurityRequirement.class, io.smallrye.openapi.internal.models.security.SecurityRequirement::new);
            registry.put(SecurityScheme.class, io.smallrye.openapi.internal.models.security.SecurityScheme::new);
            registry.put(Server.class, io.smallrye.openapi.internal.models.servers.Server::new);
            registry.put(ServerVariable.class, io.smallrye.openapi.internal.models.servers.ServerVariable::new);
            registry.put(Tag.class, io.smallrye.openapi.internal.models.tags.Tag::new);
            registry.put(XML.class, io.smallrye.openapi.internal.models.media.XML::new);
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
