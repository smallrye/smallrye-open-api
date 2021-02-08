package io.smallrye.openapi.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Check that the html gets created correctly
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class IndexCreatorTest {

    @Test
    public void testCreateDefault() throws IOException {
        byte[] indexHtml = IndexHtmlCreator.createIndexHtml();
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));
        assertTrue(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"theme-feeling-blue.css\" >"));
        assertTrue(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" >"));
        assertTrue(s.contains("url: '/openapi',"));
        assertTrue(s.contains("<img src='logo.png' alt='SmallRye OpenAPI UI'"));
        assertTrue(s.contains("dom_id: '#swagger-ui',"));
        assertTrue(s.contains("deepLinking: true,"));
    }

    @Test
    public void testCreateVanilla() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.logoHref, null);
        options.put(Option.themeHref, null);

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));
        assertFalse(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"theme-feeling-blue.css\" >"));
        assertTrue(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" >"));
        assertTrue(s.contains("url: '/openapi',"));
        assertFalse(s.contains("<img src='logo.png' alt='SmallRye OpenAPI UI'"));
        assertTrue(s.contains("dom_id: '#swagger-ui',"));
        assertTrue(s.contains("deepLinking: true,"));
    }

    @Test
    public void testCreateWithStringBooleanOption() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.syntaxHighlight, "false");
        options.put(Option.filter, "bla");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));
        assertTrue(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"theme-feeling-blue.css\" >"));
        assertTrue(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" >"));
        assertTrue(s.contains("url: '/openapi',"));
        assertTrue(s.contains("<img src='logo.png' alt='SmallRye OpenAPI UI'"));
        assertTrue(s.contains("dom_id: '#swagger-ui',"));
        assertTrue(s.contains("deepLinking: true,"));
        assertTrue(s.contains("filter: 'bla',"));
        assertTrue(s.contains("syntaxHighlight: false,"));
    }

    @Test
    public void testCreateWithMultipleUrls() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.themeHref, ThemeHref.newspaper.toString());

        Map<String, String> urls = new HashMap<>();
        urls.put("Default", "/swagger");
        urls.put("Production", "/api");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(urls, "Production", options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));
        assertTrue(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"theme-newspaper.css\" >"));
        assertTrue(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" >"));
        assertFalse(s.contains("url: '/openapi',"));
        assertTrue(s.contains("<img src='logo.png' alt='SmallRye OpenAPI UI'"));
        assertTrue(s.contains("dom_id: '#swagger-ui',"));
        assertTrue(s.contains("deepLinking: true,"));
        assertTrue(s.contains("urls: [{url: \"/api\", name: \"Production\"},{url: \"/swagger\", name: \"Default\"}],"));
        assertTrue(s.contains("\"urls.primaryName\": 'Production',"));
    }

    @Test
    public void testCreateWithMultipleUrl() throws IOException {
        Map<String, String> urls = new HashMap<>();
        urls.put("Default", "/closeapi");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(urls, "Close", null);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));
        assertTrue(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"theme-feeling-blue.css\" >"));
        assertTrue(s.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" >"));
        assertTrue(s.contains("url: '/closeapi',"));
        assertTrue(s.contains("<img src='logo.png' alt='SmallRye OpenAPI UI'"));
        assertTrue(s.contains("dom_id: '#swagger-ui',"));
        assertTrue(s.contains("deepLinking: true,"));
        assertFalse(s.contains("urls.primaryName: 'Close',"));
    }

    @Test
    public void testCreateWithInitOAuth() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.oauthClientId, "your-client-id");
        options.put(Option.oauthClientSecret, "your-client-secret-if-required");
        options.put(Option.oauthRealm, "your-realms");
        options.put(Option.oauthAppName, "your-app-name");
        options.put(Option.oauthScopeSeparator, " ");
        options.put(Option.oauthScopes, "openid profile");
        options.put(Option.oauthAdditionalQueryStringParams, "{test: \"hello\"}");
        options.put(Option.oauthUsePkceWithAuthorizationCodeGrant, "true");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));

        assertTrue(s.contains("clientId: 'your-client-id'"));
        assertTrue(s.contains("clientSecret: 'your-client-secret-if-required'"));
        assertTrue(s.contains("realm: 'your-realms'"));
        assertTrue(s.contains("appName: 'your-app-name'"));
        assertTrue(s.contains("scopeSeparator: ' '"));
        assertTrue(s.contains("scopes: 'openid profile'"));
        assertTrue(s.contains("additionalQueryStringParams: {test: \"hello\"}"));
        assertTrue(s.contains("usePkceWithAuthorizationCodeGrant: true"));
    }

    @Test
    public void testCreateWithPreauthorizeBasic() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.preauthorizeBasicAuthDefinitionKey, "basicAuth");
        options.put(Option.preauthorizeBasicUsername, "username");
        options.put(Option.preauthorizeBasicPassword, "password");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));
        assertTrue(s.contains("ui.preauthorizeBasic('basicAuth', 'username', 'password');"));
    }

    @Test
    public void testCreateWithPreauthorizeApiKey() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.preauthorizeApiKeyAuthDefinitionKey, "api_key");
        options.put(Option.preauthorizeApiKeyApiKeyValue, "abcde12345");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));
        assertTrue(s.contains("ui.preauthorizeApiKey('api_key', 'abcde12345');"));
    }

    @Test
    public void testCreateWithPreauthorizeBoth() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.preauthorizeBasicAuthDefinitionKey, "basicAuth");
        options.put(Option.preauthorizeBasicUsername, "username");
        options.put(Option.preauthorizeBasicPassword, "password");
        options.put(Option.preauthorizeApiKeyAuthDefinitionKey, "api_key");
        options.put(Option.preauthorizeApiKeyApiKeyValue, "abcde12345");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));
        assertTrue(s.contains("ui.preauthorizeApiKey('api_key', 'abcde12345');"));
        assertTrue(s.contains("<title>SmallRye OpenAPI UI</title>"));
        assertTrue(s.contains("ui.preauthorizeBasic('basicAuth', 'username', 'password');"));
    }
}