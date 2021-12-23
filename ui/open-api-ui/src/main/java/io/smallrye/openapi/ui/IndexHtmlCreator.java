package io.smallrye.openapi.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an index html based on some options
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class IndexHtmlCreator {

    private IndexHtmlCreator() {
    }

    public static byte[] createIndexHtml() throws IOException {
        return createIndexHtml(null);
    }

    public static byte[] createIndexHtml(Map<Option, String> options) throws IOException {
        return createIndexHtml(null, null, options);
    }

    public static byte[] createIndexHtml(Map<String, String> urls, String urlsPrimaryName, Map<Option, String> options)
            throws IOException {
        // First add the default that is not overridden
        options = populateDefaults(options);
        // Next sort out the url/urls
        addUrlSection(urls, urlsPrimaryName, options);
        // Add OAuth section
        addOAuthSection(options);
        // Add Preauth section
        addPreauthorizeSection(options);

        try (InputStream input = IndexHtmlCreator.class.getClassLoader()
                .getResourceAsStream("META-INF/resources/template/index.html");
                InputStreamReader streamreader = new InputStreamReader(input, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamreader);
                StringWriter writer = new StringWriter()) {

            String str;
            while ((str = reader.readLine()) != null) {
                str = replace(str, urls, urlsPrimaryName, options);
                if (str != null) {
                    writer.write(str + "\n");
                }
            }
            String result = writer.toString();
            return result.getBytes(StandardCharsets.UTF_8);
        }
    }

    private static String replace(String line, Map<String, String> urls, String urlsPrimaryName, Map<Option, String> options) {
        if (line.contains(VAR_BEGIN)) {
            Option variableOption = getVariable(line);
            if (variableOption != null) {
                if (options != null && options.containsKey(variableOption)) {
                    String replacement = options.get(variableOption);
                    if (replacement == null) {
                        // Special case for oauth2RedirectUrl
                        if (variableOption.equals("oauth2RedirectUrl")) {
                            return "-";
                        }
                        // You want to remove this line
                        return null;
                    } else {
                        // Some properties can be boolean or String, if String we need to add '
                        replacement = replacement.trim();
                        if (BOOLEAN_OR_STRING_KEYS.contains(variableOption)) {
                            if (!replacement.equals("true") && !replacement.equals("false")) {
                                replacement = "'" + replacement + "'";
                            }
                        }
                        // Some properties can be a String or a function, if String we need to add '
                        replacement = replacement.trim();
                        if (STRING_OR_FUNCTION_KEYS.contains(variableOption)) {
                            if (!replacement.startsWith("function")) {
                                replacement = "'" + replacement + "'";
                            }
                        }
                        // Some properties are string arrays, and we need to add the ' per element
                        if (STRING_ARRAY_KEYS.contains(variableOption)) {
                            List<String> newArray = new ArrayList<>();
                            String[] parts = replacement.replace("[", "").replace("]", "").split(",");
                            for (String part : parts) {
                                newArray.add("'" + part.trim() + "'");
                            }
                            replacement = Arrays.toString(newArray.toArray(new String[] {}));
                        }

                        line = line.replace(VAR_BEGIN + variableOption + VAR_END, replacement);
                    }
                }
                // Check if there is more varibles
                return replace(line, urls, urlsPrimaryName, options);
            }
        }
        return line;
    }

    private static Option getVariable(String line) {
        try {
            String stringValue = line.substring(line.indexOf(VAR_BEGIN) + 2, line.indexOf(VAR_END));
            return Option.valueOf(stringValue);
        } catch (IllegalArgumentException iae) {
            // Quitly fall through (maybe you want the var there ?)
            return null;
        }
    }

    private static Map<Option, String> populateDefaults(Map<Option, String> options) {
        if (options == null) {
            options = new HashMap<>(DEFAULT_OPTIONS);
        } else {
            for (Map.Entry<Option, String> defaultVar : DEFAULT_OPTIONS.entrySet()) {
                if (!options.containsKey(defaultVar.getKey())) {
                    options.put(defaultVar.getKey(), defaultVar.getValue());
                }
            }
        }
        return options;
    }

    private static void addPreauthorizeSection(Map<Option, String> options) {
        if (options.containsKey(Option.preauthorizeSection) && options.get(Option.preauthorizeSection) != null) {
            options.put(Option.preauthorizeSection, options.get(Option.preauthorizeSection));
        } else if ((options.containsKey(Option.preauthorizeBasicAuthDefinitionKey) &&
                options.containsKey(Option.preauthorizeBasicUsername) &&
                options.containsKey(Option.preauthorizeBasicPassword))
                ||
                (options.containsKey(Option.preauthorizeApiKeyAuthDefinitionKey) &&
                        options.containsKey(Option.preauthorizeApiKeyApiKeyValue))) {

            try (StringWriter sw = new StringWriter()) {

                sw.write("onComplete: function() {\n");
                if (options.containsKey(Option.preauthorizeBasicAuthDefinitionKey) &&
                        options.containsKey(Option.preauthorizeBasicUsername) &&
                        options.containsKey(Option.preauthorizeBasicPassword)) {
                    sw.write("\t\t\tui.preauthorizeBasic('" + options.get(Option.preauthorizeBasicAuthDefinitionKey) + "', '"
                            + options.get(Option.preauthorizeBasicUsername) + "', '"
                            + options.get(Option.preauthorizeBasicPassword) + "');\n");
                }
                if (options.containsKey(Option.preauthorizeApiKeyAuthDefinitionKey) &&
                        options.containsKey(Option.preauthorizeApiKeyApiKeyValue)) {
                    sw.write("\t\t\tui.preauthorizeApiKey('" + options.get(Option.preauthorizeApiKeyAuthDefinitionKey) + "', '"
                            + options.get(Option.preauthorizeApiKeyApiKeyValue) + "');\n");
                }
                sw.write("\t\t}\n");

                String preauthorizeSection = sw.toString();

                options.put(Option.preauthorizeSection, preauthorizeSection);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static void addOAuthSection(Map<Option, String> options) {
        if (options.containsKey(Option.initOAuthSection) && options.get(Option.initOAuthSection) != null) {
            options.put(Option.initOAuthSection, options.get(Option.initOAuthSection));
        } else if (options.containsKey(Option.oauthClientId) ||
                options.containsKey(Option.oauthClientSecret) ||
                options.containsKey(Option.oauthRealm) ||
                options.containsKey(Option.oauthAppName) ||
                options.containsKey(Option.oauthScopeSeparator) ||
                options.containsKey(Option.oauthScopes) ||
                options.containsKey(Option.oauthUseBasicAuthenticationWithAccessCodeGrant) ||
                options.containsKey(Option.oauthUsePkceWithAuthorizationCodeGrant)) {

            try (StringWriter sw = new StringWriter()) {

                sw.write("ui.initOAuth({\n");
                if (options.containsKey(Option.oauthClientId)) {
                    sw.write("\t\tclientId: '" + options.get(Option.oauthClientId) + "',\n");
                }
                if (options.containsKey(Option.oauthClientSecret)) {
                    sw.write("\t\tclientSecret: '" + options.get(Option.oauthClientSecret) + "',\n");
                }
                if (options.containsKey(Option.oauthRealm)) {
                    sw.write("\t\trealm: '" + options.get(Option.oauthRealm) + "',\n");
                }
                if (options.containsKey(Option.oauthAppName)) {
                    sw.write("\t\tappName: '" + options.get(Option.oauthAppName) + "',\n");
                }
                if (options.containsKey(Option.oauthScopeSeparator)) {
                    sw.write("\t\tscopeSeparator: '" + options.get(Option.oauthScopeSeparator) + "',\n");
                }
                if (options.containsKey(Option.oauthScopes)) {
                    sw.write("\t\tscopes: '" + options.get(Option.oauthScopes) + "',\n");
                }
                if (options.containsKey(Option.oauthAdditionalQueryStringParams)) {
                    sw.write(
                            "\t\tadditionalQueryStringParams: " + options.get(Option.oauthAdditionalQueryStringParams) + ",\n");
                }
                if (options.containsKey(Option.oauthUseBasicAuthenticationWithAccessCodeGrant)) {
                    sw.write("\t\tuseBasicAuthenticationWithAccessCodeGrant: "
                            + options.get(Option.oauthUseBasicAuthenticationWithAccessCodeGrant) + ",\n");
                }
                if (options.containsKey(Option.oauthUsePkceWithAuthorizationCodeGrant)) {
                    sw.write(
                            "\t\tusePkceWithAuthorizationCodeGrant: "
                                    + options.get(Option.oauthUsePkceWithAuthorizationCodeGrant)
                                    + "\n");
                }
                sw.write("\t})");

                String initOAuth = sw.toString();

                options.put(Option.initOAuthSection, initOAuth);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static void addUrlSection(Map<String, String> urls, String urlsPrimaryName, Map<Option, String> options) {
        // If you added a urlSection, we assume you know what you are doing
        if (!options.containsKey(Option.urlSection)) {
            if (urls == null || urls.isEmpty()) {
                if (options.containsKey(Option.url)) {
                    // Add one url with your value
                    options.put(Option.urlSection, String.format(URL_FORMAT, options.get(Option.url)));
                } else {
                    // Add one url with the default value
                    options.put(Option.urlSection, String.format(URL_FORMAT, "/openapi"));
                }
            } else if (urls.size() == 1) {
                // Add one url with your value
                options.put(Option.urlSection, String.format(URL_FORMAT, urls.values().iterator().next()));
            } else {
                // Add multiple urls
                Set<Map.Entry<String, String>> urlsSet = urls.entrySet();
                List<String> urlsLines = new ArrayList<>();
                for (Map.Entry<String, String> kv : urlsSet) {
                    urlsLines.add(String.format(URLS_ENTRY_FORMAT, kv.getValue(), kv.getKey()));
                }
                String urlSection = "urls: [" + String.join(",", urlsLines.toArray(new String[] {})) + "]";

                // Check the name
                if (urlsPrimaryName != null) {
                    urlSection = urlSection + ",\n\t\t \"urls.primaryName\": '" + urlsPrimaryName + "'";
                }
                options.put(Option.urlSection, urlSection);
            }
        }
    }

    // Some properties can be a boolean or a String. To render correct we need to handle those specially
    private static final List<Option> BOOLEAN_OR_STRING_KEYS = Arrays
            .asList(new Option[] { Option.filter, Option.syntaxHighlight });

    // Some properties can be a String or a function. To render correct we need to handle those specially
    private static final List<Option> STRING_OR_FUNCTION_KEYS = Arrays
            .asList(new Option[] { Option.operationsSorter, Option.tagsSorter });

    // Some properties can be a String arrays. To render correct we need to handle those specially
    private static final List<Option> STRING_ARRAY_KEYS = Arrays
            .asList(new Option[] { Option.supportedSubmitMethods, Option.requestCurlOptions });

    private static final String VAR_BEGIN = "${";
    private static final String VAR_END = "}";

    private static final Map<Option, String> DEFAULT_OPTIONS = new HashMap<>();
    private static final String DEFAULT_URLS_PRIMARY_NAME = "Default";
    private static final String URL_FORMAT = "url: '%s'";
    private static final String URLS_ENTRY_FORMAT = "{url: \"%s\", name: \"%s\"}";

    static {

        DEFAULT_OPTIONS.put(Option.url, "/openapi");
        DEFAULT_OPTIONS.put(Option.title, "SmallRye OpenAPI UI");
        DEFAULT_OPTIONS.put(Option.selfHref, "/openapi-ui");
        DEFAULT_OPTIONS.put(Option.backHref, "/openapi-ui");
        DEFAULT_OPTIONS.put(Option.themeHref, ThemeHref.feeling_blue.toString());
        DEFAULT_OPTIONS.put(Option.logoHref, "logo.png");
        DEFAULT_OPTIONS.put(Option.styleHref, "style.css");
        DEFAULT_OPTIONS.put(Option.footer, null);
        DEFAULT_OPTIONS.put(Option.domId, "#swagger-ui");

        // Display section
        DEFAULT_OPTIONS.put(Option.deepLinking, "true");
        DEFAULT_OPTIONS.put(Option.displayOperationId, null);
        DEFAULT_OPTIONS.put(Option.defaultModelsExpandDepth, null);
        DEFAULT_OPTIONS.put(Option.defaultModelExpandDepth, null);
        DEFAULT_OPTIONS.put(Option.defaultModelRendering, null);
        DEFAULT_OPTIONS.put(Option.displayRequestDuration, null);
        DEFAULT_OPTIONS.put(Option.docExpansion, null);
        DEFAULT_OPTIONS.put(Option.filter, null);
        DEFAULT_OPTIONS.put(Option.maxDisplayedTags, null);
        DEFAULT_OPTIONS.put(Option.operationsSorter, null);
        DEFAULT_OPTIONS.put(Option.showExtensions, null);
        DEFAULT_OPTIONS.put(Option.showCommonExtensions, null);
        DEFAULT_OPTIONS.put(Option.tagsSorter, null);
        DEFAULT_OPTIONS.put(Option.onComplete, null);
        DEFAULT_OPTIONS.put(Option.syntaxHighlight, null);

        // Network section 
        DEFAULT_OPTIONS.put(Option.oauth2RedirectUrl, null);
        DEFAULT_OPTIONS.put(Option.requestInterceptor, null);
        DEFAULT_OPTIONS.put(Option.requestCurlOptions, null);
        DEFAULT_OPTIONS.put(Option.responseInterceptor, null);
        DEFAULT_OPTIONS.put(Option.showMutatedRequest, null);
        DEFAULT_OPTIONS.put(Option.supportedSubmitMethods, null);
        DEFAULT_OPTIONS.put(Option.validatorUrl, null);
        DEFAULT_OPTIONS.put(Option.withCredentials, null);

        // Macros
        DEFAULT_OPTIONS.put(Option.modelPropertyMacro, null);
        DEFAULT_OPTIONS.put(Option.parameterMacro, null);

        // Authorization
        DEFAULT_OPTIONS.put(Option.persistAuthorization, null);

        // initOAuth
        DEFAULT_OPTIONS.put(Option.initOAuthSection, null);

        // preauthorize
        DEFAULT_OPTIONS.put(Option.preauthorizeSection, null);

        DEFAULT_OPTIONS.put(Option.layout, "StandaloneLayout");
        DEFAULT_OPTIONS.put(Option.plugins, "[SwaggerUIBundle.plugins.DownloadUrl]");
        DEFAULT_OPTIONS.put(Option.presets, "[SwaggerUIBundle.presets.apis,SwaggerUIStandalonePreset]");
    }

}
