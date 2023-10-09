package io.smallrye.openapi.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Check that the html gets created correctly
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class IndexCreatorTest {

    @Test
    void testCreateDefault() throws IOException {
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
        assertFalse(s.contains("queryConfigEnabled"));
        assertFalse(s.contains("tryItOutEnabled"));
    }

    @Test
    void testCreateVanilla() throws IOException {
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
    void testCreateWithStringBooleanOption() throws IOException {
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
    void testCreateWithMultipleUrls() throws IOException {
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
    void testCreateWithMultipleUrl() throws IOException {
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
    void testCreateWithInitOAuth() throws IOException {
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
    void testCreateWithPreauthorizeBasic() throws IOException {
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
    void testCreateWithPreauthorizeApiKey() throws IOException {
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
    void testCreateWithPreauthorizeBoth() throws IOException {
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

    @Test
    void testOauth2RedirectReplacement() throws IOException {
        String title = "Test Title";
        Map<Option, String> options = new HashMap<>();
        options.put(Option.title, title);
        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);

        String html = new String(indexHtml);

        assertTrue(html.contains("var oar"), "Missing declaration of 'oar'");
    }

    @Test
    void testCreateWithSyntaxHighlightBoolean() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.syntaxHighlight, "false");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("syntaxHighlight: false,"));
    }

    @Test
    void testCreateWithSyntaxHighlightObject() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.syntaxHighlight, "{ activated: true, theme: \"monokai\" }");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("syntaxHighlight: { activated: true, theme: \"monokai\" },"));
    }

    @Test
    void testCreateWithTryItOutEnabledBoolean() throws IOException {
        Map<Option, String> options = new HashMap<>();
        options.put(Option.tryItOutEnabled, "true");

        byte[] indexHtml = IndexHtmlCreator.createIndexHtml(options);
        assertNotNull(indexHtml);

        String s = new String(indexHtml);

        assertTrue(s.contains("tryItOutEnabled: true,"));
    }
}
