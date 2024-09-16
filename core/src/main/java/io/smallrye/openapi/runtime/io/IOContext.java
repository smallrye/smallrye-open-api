package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.io.callbacks.CallbackIO;
import io.smallrye.openapi.runtime.io.callbacks.CallbackOperationIO;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.headers.HeaderIO;
import io.smallrye.openapi.runtime.io.info.ContactIO;
import io.smallrye.openapi.runtime.io.info.InfoIO;
import io.smallrye.openapi.runtime.io.info.LicenseIO;
import io.smallrye.openapi.runtime.io.links.LinkIO;
import io.smallrye.openapi.runtime.io.links.LinkParameterIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.media.DiscriminatorIO;
import io.smallrye.openapi.runtime.io.media.EncodingIO;
import io.smallrye.openapi.runtime.io.media.ExampleObjectIO;
import io.smallrye.openapi.runtime.io.media.MediaTypeIO;
import io.smallrye.openapi.runtime.io.media.SchemaIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.parameters.RequestBodyIO;
import io.smallrye.openapi.runtime.io.responses.APIResponseIO;
import io.smallrye.openapi.runtime.io.responses.APIResponsesIO;
import io.smallrye.openapi.runtime.io.security.OAuthFlowIO;
import io.smallrye.openapi.runtime.io.security.OAuthFlowsIO;
import io.smallrye.openapi.runtime.io.security.OAuthScopeIO;
import io.smallrye.openapi.runtime.io.security.SecurityIO;
import io.smallrye.openapi.runtime.io.security.SecurityRequirementIO;
import io.smallrye.openapi.runtime.io.security.SecurityRequirementsSetIO;
import io.smallrye.openapi.runtime.io.security.SecuritySchemeIO;
import io.smallrye.openapi.runtime.io.servers.ServerIO;
import io.smallrye.openapi.runtime.io.servers.ServerVariableIO;
import io.smallrye.openapi.runtime.io.tags.TagIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class IOContext<V, A extends V, O extends V, AB, OB> {

    /**
     * The major.minor version of OpenAPI being used for (de-)serizalization
     */
    public enum OpenApiVersion {
        V3_0,
        V3_1;

        public static OpenApiVersion fromString(String version) {
            if (version != null && version.startsWith("3.0")) {
                return V3_0;
            } else {
                return V3_1;
            }
        }
    }

    private AnnotationScannerContext scannerContext;
    private JsonIO<V, A, O, AB, OB> jsonIO;

    private OpenApiVersion openApiVersion;

    private final ComponentsIO<V, A, O, AB, OB> componentsIO = new ComponentsIO<>(this);
    private final ExternalDocumentationIO<V, A, O, AB, OB> extDocIO = new ExternalDocumentationIO<>(this);
    private final OpenAPIDefinitionIO<V, A, O, AB, OB> openApiDefinitionIO = new OpenAPIDefinitionIO<>(this);
    private final OperationIO<V, A, O, AB, OB> operationIO = new OperationIO<>(this);
    private final CallbackOperationIO<V, A, O, AB, OB> callbackOperationIO = new CallbackOperationIO<>(this);
    private final PathItemOperationIO<V, A, O, AB, OB> pathItemOperationIO = new PathItemOperationIO<>(this);
    private final PathItemIO<V, A, O, AB, OB> pathItemIO = new PathItemIO<>(this);
    private final PathsIO<V, A, O, AB, OB> pathsIO = new PathsIO<>(this);
    private final CallbackIO<V, A, O, AB, OB> callbackIO = new CallbackIO<>(this);
    private final ExtensionIO<V, A, O, AB, OB> extensionIO = new ExtensionIO<>(this);
    private final HeaderIO<V, A, O, AB, OB> headerIO = new HeaderIO<>(this);
    private final ContactIO<V, A, O, AB, OB> contactIO = new ContactIO<>(this);
    private final InfoIO<V, A, O, AB, OB> infoIO = new InfoIO<>(this);
    private final LicenseIO<V, A, O, AB, OB> licenseIO = new LicenseIO<>(this);
    private final LinkIO<V, A, O, AB, OB> linkIO = new LinkIO<>(this);
    private final LinkParameterIO<V, A, O, AB, OB> linkParameterIO = new LinkParameterIO<>(this);
    private final ContentIO<V, A, O, AB, OB> contentIO = new ContentIO<>(this);
    private final DiscriminatorIO<V, A, O, AB, OB> discriminatorIO = new DiscriminatorIO<>(this);
    private final EncodingIO<V, A, O, AB, OB> encodingIO = new EncodingIO<>(this);
    private final ExampleObjectIO<V, A, O, AB, OB> exampleObjectIO = new ExampleObjectIO<>(this);
    private final MediaTypeIO<V, A, O, AB, OB> mediaTypeIO = new MediaTypeIO<>(this);
    private final SchemaIO<V, A, O, AB, OB> schemaIO = new SchemaIO<>(this);
    private final ParameterIO<V, A, O, AB, OB> parameterIO = new ParameterIO<>(this);
    private final RequestBodyIO<V, A, O, AB, OB> requestBodyIO = new RequestBodyIO<>(this);
    private final APIResponseIO<V, A, O, AB, OB> apiResponseIO = new APIResponseIO<>(this);
    private final APIResponsesIO<V, A, O, AB, OB> apiResponsesIO = new APIResponsesIO<>(this);
    private final OAuthFlowIO<V, A, O, AB, OB> oauthFlowIO = new OAuthFlowIO<>(this);
    private final OAuthFlowsIO<V, A, O, AB, OB> oauthFlowsIO = new OAuthFlowsIO<>(this);
    private final OAuthScopeIO<V, A, O, AB, OB> oauthScopeIO = new OAuthScopeIO<>(this);
    private final SecurityIO<V, A, O, AB, OB> securityIO = new SecurityIO<>(this);
    private final SecurityRequirementIO<V, A, O, AB, OB> securityRequirementIO = new SecurityRequirementIO<>(this);
    private final SecurityRequirementsSetIO<V, A, O, AB, OB> securityRequirementsSetIO = new SecurityRequirementsSetIO<>(this);
    private final SecuritySchemeIO<V, A, O, AB, OB> securitySchemeIO = new SecuritySchemeIO<>(this);
    private final ServerIO<V, A, O, AB, OB> serverIO = new ServerIO<>(this);
    private final ServerVariableIO<V, A, O, AB, OB> serverVariableIO = new ServerVariableIO<>(this);
    private final TagIO<V, A, O, AB, OB> tagIO = new TagIO<>(this);

    public static <V, A extends V, O extends V, AB, OB> IOContext<V, A, O, AB, OB> empty() {
        return new IOContext<>(null, null);
    }

    public static <V, A extends V, O extends V, AB, OB> IOContext<V, A, O, AB, OB> forJson(JsonIO<V, A, O, AB, OB> jsonIO) {
        return new IOContext<>(null, jsonIO);
    }

    public static IOContext<?, ?, ?, ?, ?> forScanning(AnnotationScannerContext context) { // NOSONAR
        return new IOContext<>(context, null);
    }

    private IOContext(AnnotationScannerContext context, JsonIO<V, A, O, AB, OB> jsonIO) {
        this.scannerContext = context;
        this.jsonIO = jsonIO;
    }

    public OpenApiVersion openApiVersion() {
        return Optional.ofNullable(openApiVersion)
                .orElseGet(() -> Optional.ofNullable(scannerContext)
                        .map(AnnotationScannerContext::getConfig)
                        .map(OpenApiConfig::getOpenApiVersion)
                        .map(OpenApiVersion::fromString)
                        .orElse(OpenApiVersion.V3_1));
    }

    public void setOpenApiVersion(OpenApiVersion version) {
        this.openApiVersion = version;
    }

    public AnnotationScannerContext scannerContext() {
        return scannerContext;
    }

    public void scannerContext(AnnotationScannerContext scannerContext) {
        this.scannerContext = scannerContext;
    }

    public JsonIO<V, A, O, AB, OB> jsonIO() {
        return jsonIO;
    }

    public void jsonIO(JsonIO<V, A, O, AB, OB> jsonIO) {
        this.jsonIO = jsonIO;
    }

    public ComponentsIO<V, A, O, AB, OB> componentsIO() {
        return componentsIO;
    }

    public ExternalDocumentationIO<V, A, O, AB, OB> extDocIO() {
        return extDocIO;
    }

    public OpenAPIDefinitionIO<V, A, O, AB, OB> openApiDefinitionIO() {
        return openApiDefinitionIO;
    }

    public OperationIO<V, A, O, AB, OB> operationIO() {
        return operationIO;
    }

    public PathItemOperationIO<V, A, O, AB, OB> pathItemOperationIO() {
        return pathItemOperationIO;
    }

    public PathItemIO<V, A, O, AB, OB> pathItemIO() {
        return pathItemIO;
    }

    public PathsIO<V, A, O, AB, OB> pathsIO() {
        return pathsIO;
    }

    public CallbackIO<V, A, O, AB, OB> callbackIO() {
        return callbackIO;
    }

    public CallbackOperationIO<V, A, O, AB, OB> callbackOperationIO() {
        return callbackOperationIO;
    }

    public ExtensionIO<V, A, O, AB, OB> extensionIO() {
        return extensionIO;
    }

    public HeaderIO<V, A, O, AB, OB> headerIO() {
        return headerIO;
    }

    public ContactIO<V, A, O, AB, OB> contactIO() {
        return contactIO;
    }

    public InfoIO<V, A, O, AB, OB> infoIO() {
        return infoIO;
    }

    public LicenseIO<V, A, O, AB, OB> licenseIO() {
        return licenseIO;
    }

    public LinkIO<V, A, O, AB, OB> linkIO() {
        return linkIO;
    }

    public LinkParameterIO<V, A, O, AB, OB> linkParameterIO() {
        return linkParameterIO;
    }

    public ContentIO<V, A, O, AB, OB> contentIO() {
        return contentIO;
    }

    public DiscriminatorIO<V, A, O, AB, OB> discriminatorIO() {
        return discriminatorIO;
    }

    public EncodingIO<V, A, O, AB, OB> encodingIO() {
        return encodingIO;
    }

    public ExampleObjectIO<V, A, O, AB, OB> exampleObjectIO() {
        return exampleObjectIO;
    }

    public MediaTypeIO<V, A, O, AB, OB> mediaTypeIO() {
        return mediaTypeIO;
    }

    public SchemaIO<V, A, O, AB, OB> schemaIO() {
        return schemaIO;
    }

    public ParameterIO<V, A, O, AB, OB> parameterIO() {
        return parameterIO;
    }

    public RequestBodyIO<V, A, O, AB, OB> requestBodyIO() {
        return requestBodyIO;
    }

    public APIResponseIO<V, A, O, AB, OB> apiResponseIO() {
        return apiResponseIO;
    }

    public APIResponsesIO<V, A, O, AB, OB> apiResponsesIO() {
        return apiResponsesIO;
    }

    public OAuthFlowIO<V, A, O, AB, OB> oauthFlowIO() {
        return oauthFlowIO;
    }

    public OAuthFlowsIO<V, A, O, AB, OB> oauthFlowsIO() {
        return oauthFlowsIO;
    }

    public OAuthScopeIO<V, A, O, AB, OB> oauthScopeIO() {
        return oauthScopeIO;
    }

    public SecurityIO<V, A, O, AB, OB> securityIO() {
        return securityIO;
    }

    public SecurityRequirementIO<V, A, O, AB, OB> securityRequirementIO() {
        return securityRequirementIO;
    }

    public SecurityRequirementsSetIO<V, A, O, AB, OB> securityRequirementsSetIO() {
        return securityRequirementsSetIO;
    }

    public SecuritySchemeIO<V, A, O, AB, OB> securitySchemeIO() {
        return securitySchemeIO;
    }

    public ServerIO<V, A, O, AB, OB> serverIO() {
        return serverIO;
    }

    public ServerVariableIO<V, A, O, AB, OB> serverVariableIO() {
        return serverVariableIO;
    }

    public TagIO<V, A, O, AB, OB> tagIO() {
        return tagIO;
    }

}
