package io.smallrye.openapi.ui;

/**
 * All the available options
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum Option {
    scriptsSection,
    scripts,
    url,
    urlSection,
    title,
    selfHref,
    backHref,
    themeHref,
    logoHref,
    styleHref,
    footer,
    domId,
    deepLinking,
    displayOperationId,
    defaultModelsExpandDepth,
    defaultModelExpandDepth,
    defaultModelRendering,
    displayRequestDuration,
    docExpansion,
    filter,
    maxDisplayedTags,
    operationsSorter,
    showExtensions,
    showCommonExtensions,
    tagsSorter,
    onComplete,
    syntaxHighlight,
    oauth2RedirectUrl,
    requestInterceptor,
    requestCurlOptions,
    responseInterceptor,
    showMutatedRequest,
    supportedSubmitMethods,
    validatorUrl,
    withCredentials,
    modelPropertyMacro,
    parameterMacro,
    persistAuthorization,
    layout,
    plugins,
    presets,
    tryItOutEnabled,
    // OAuth related
    initOAuthSection,
    oauthClientId,
    oauthClientSecret,
    oauthRealm,
    oauthAppName,
    oauthScopeSeparator,
    oauthScopes,
    oauthAdditionalQueryStringParams,
    oauthUseBasicAuthenticationWithAccessCodeGrant,
    oauthUsePkceWithAuthorizationCodeGrant,
    // Preauthorize
    preauthorizeSection,
    preauthorizeBasicAuthDefinitionKey,
    preauthorizeBasicUsername,
    preauthorizeBasicPassword,
    preauthorizeApiKeyAuthDefinitionKey,
    queryConfigEnabled,
    preauthorizeApiKeyApiKeyValue
}