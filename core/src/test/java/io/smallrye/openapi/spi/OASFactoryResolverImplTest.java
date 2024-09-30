package io.smallrye.openapi.spi;

import org.eclipse.microprofile.openapi.OASFactory;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author eric.wittmann@gmail.com
 */
class OASFactoryResolverImplTest {

    /**
     * Test method for
     * {@link OASFactoryResolverImpl#createObject(java.lang.Class)}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void testCreateObject_All() {
        Class modelClasses[] = { APIResponse.class, APIResponses.class, Callback.class, Components.class,
                Contact.class, Content.class, Discriminator.class, Encoding.class, Example.class,
                ExternalDocumentation.class, Header.class, Info.class, License.class, Link.class, MediaType.class,
                OAuthFlow.class, OAuthFlows.class, OpenAPI.class, Operation.class, Parameter.class, PathItem.class,
                Paths.class, RequestBody.class, Schema.class, SecurityRequirement.class,
                SecurityScheme.class, Server.class, ServerVariable.class, Tag.class, XML.class };
        for (Class modelClass : modelClasses) {
            Constructible object = OASFactory.createObject(modelClass);
            Assertions.assertNotNull(object);
        }
    }

    /**
     * Test method for
     * {@link OASFactoryResolverImpl#createObject(java.lang.Class)}.
     */
    @Test
    void testCreateObject_License() {
        License license = OASFactory.createObject(License.class).name("Test License").url("urn:test-url");
        Assertions.assertNotNull(license);
        Assertions.assertEquals(io.smallrye.openapi.internal.models.info.License.class, license.getClass());
        Assertions.assertEquals("Test License", license.getName());
        Assertions.assertEquals("urn:test-url", license.getUrl());
    }

    /**
     * Test method for
     * {@link OASFactoryResolverImpl#createObject(java.lang.Class)}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void testCreateObject_RTE() {
        Class c = String.class;
        try {
            OASFactory.createObject(c);
            Assertions.fail("Expected a runtime error.");
        } catch (RuntimeException e) {
            Assertions.assertEquals("SROAP09000: Class 'java.lang.String' is not Constructible.", e.getMessage());
        }
    }

}
