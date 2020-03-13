package io.smallrye.openapi.api.constants;

import java.util.function.Supplier;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callbacks;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.extensions.Extensions;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.servers.Servers;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.jandex.DotName;

/**
 * Constants related to MicroProfile OpenAPI.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class MPOpenApiConstants {

    // OpenAPI Definition (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#openapi-object)
    public static class OPEN_API_DEFINITION {
        public static final DotName TYPE_OPEN_API_DEFINITION = DotName.createSimple(OpenAPIDefinition.class.getName());
        public static final String PROP_OPENAPI = "openapi";
        public static final String PROP_INFO = "info";
        public static final String PROP_SERVERS = "servers";
        public static final String PROP_PATHS = "paths";
        public static final String PROP_COMPONENTS = "components";
        public static final String PROP_SECURITY = "security";
        public static final String PROP_TAGS = "tags";
        public static final String PROP_EXTERNAL_DOCS = EXTERNAL_DOCS;
    }

    // Info (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#infoObject)
    public static class INFO {
        public static final String PROP_TITLE = "title";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_TERMS_OF_SERVICE = "termsOfService";
        public static final String PROP_CONTACT = "contact";
        public static final String PROP_LICENSE = "license";
        public static final String PROP_VERSION = "version";
    }

    // Contact (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#contactObject)
    public static class CONTACT {
        public static final String PROP_EMAIL = "email";
        public static final String PROP_URL = "url";
        public static final String PROP_NAME = NAME;
    }

    // License (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#licenseObject)
    public static class LICENSE {
        public static final String PROP_URL = "url";
        public static final String PROP_NAME = NAME;
    }

    // Tag (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#tagObject)
    public static class TAG {
        public static final DotName TYPE_TAG = DotName.createSimple(Tag.class.getName());
        public static final DotName TYPE_TAGS = DotName.createSimple(Tags.class.getName());
        public static final String PROP_NAME = "name";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_EXTERNAL_DOCS = EXTERNAL_DOCS;
    }

    // Server (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverObject)
    public static class SERVER {
        public static final DotName TYPE_SERVER = DotName.createSimple(Server.class.getName());
        public static final DotName TYPE_SERVERS = DotName.createSimple(Servers.class.getName());
        public static final String PROP_URL = "url";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_VARIABLES = "variables";
    }

    // Server Variable (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverVariableObject)
    public static class SERVER_VARIABLE {
        public static final String PROP_NAME = NAME;
        public static final String PROP_ENUM = "enum";
        public static final String PROP_DEFAULT = "default";
        public static final String PROP_DESCRIPTION = "description";
        // for annotations (reserved words in Java)
        public static final String PROP_ENUMERATION = "enumeration";
        public static final String PROP_DEFAULT_VALUE = "defaultValue";
    }

    // Security (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#security-requirement-object)
    public static class SECURITYREQUIREMENT {
        public static final DotName TYPE_SECURITY_REQUIREMENT = DotName.createSimple(SecurityRequirement.class.getName());
        public static final DotName TYPE_SECURITY_REQUIREMENTS = DotName.createSimple(SecurityRequirements.class.getName());
        public static final String PROP_NAME = NAME;
        public static final String PROP_SCOPES = "scopes";
    }

    // External Documentation (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#externalDocumentationObject)
    public static class EXTERNAL_DOCUMENTATION {
        public static final String PROP_URL = "url";
        public static final String PROP_DESCRIPTION = "description";
    }

    // Components (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#componentsObject)
    public static class COMPONENTS {
        public static final String PROP_CALLBACKS = "callbacks";
        public static final String PROP_LINKS = "links";
        public static final String PROP_SECURITY_SCHEMES = "securitySchemes";
        public static final String PROP_HEADERS = "headers";
        public static final String PROP_REQUEST_BODIES = "requestBodies";
        public static final String PROP_EXAMPLES = "examples";
        public static final String PROP_PARAMETERS = "parameters";
        public static final String PROP_RESPONSES = "responses";
        public static final String PROP_SCHEMAS = "schemas";
    }

    // Callback (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#callbackObject)
    public static class CALLBACK {
        public static final DotName TYPE_CALLBACK = DotName.createSimple(Callback.class.getName());
        public static final DotName TYPE_CALLBACKS = DotName.createSimple(Callbacks.class.getName());
        public static final String PROP_CALLBACKS = "callbacks";
        public static final String PROP_NAME = NAME;
        public static final String PROP_CALLBACK_URL_EXPRESSION = "callbackUrlExpression";
        public static final String PROP_OPERATIONS = "operations";
        public static final String PROP_REF_VAR = $REF;
    }

    // Example (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#exampleObject)
    public static class EXAMPLE {
        public static final String PROP_EXAMPLES = "examples";
        public static final String PROP_NAME = NAME;
        public static final String PROP_REF_VAR = $REF;
        public static final String PROP_SUMMARY = "summary";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_VALUE = VALUE;
        public static final String PROP_EXTERNAL_VALUE = "externalValue";
    }

    // Header (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#headerObject)
    public static class HEADER {
        public static final String PROP_HEADERS = "headers";
        public static final String PROP_NAME = NAME;
        public static final String PROP_REF_VAR = $REF;
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_SCHEMA = "schema";
        public static final String PROP_REQUIRED = "required";
        public static final String PROP_DEPRECATED = "deprecated";
        public static final String PROP_ALLOW_EMPTY_VALUE = "allowEmptyValue";
        public static final String PROP_EXPLODE = "explode";
        public static final String PROP_STYLE = "style";
        public static final String PROP_EXAMPLE = "example";
        public static final String PROP_EXAMPLES = "examples";
        public static final String PROP_CONTENT = "content";
    }

    // Schema (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#schemaObject)
    public static class SCHEMA {
        public static final DotName TYPE_SCHEMA = DotName.createSimple(Schema.class.getName());
        public static final String PROP_REF_VAR = $REF;
        public static final String PROP_NAME = NAME;
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_TITLE = "title";
        public static final String PROP_DEFAULT = "default";
        public static final String PROP_MIN_PROPERTIES = "minProperties";
        public static final String PROP_MAX_PROPERTIES = "maxProperties";
        public static final String PROP_UNIQUE_ITEMS = "uniqueItems";
        public static final String PROP_MIN_ITEMS = "minItems";
        public static final String PROP_MAX_ITEMS = "maxItems";
        public static final String PROP_PATTERN = "pattern";
        public static final String PROP_MIN_LENGTH = "minLength";
        public static final String PROP_MAX_LENGTH = "maxLength";
        public static final String PROP_EXCLUSIVE_MINIMUM = "exclusiveMinimum";
        public static final String PROP_MINIMUM = "minimum";
        public static final String PROP_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
        public static final String PROP_MAXIMUM = "maximum";
        public static final String PROP_MULTIPLE_OF = "multipleOf";
        public static final String PROP_FORMAT = "format";
        public static final String PROP_REQUIRED = "required";
        public static final String PROP_ENUM = "enum";
        public static final String PROP_WRITE_ONLY = "writeOnly";
        public static final String PROP_NULLABLE = "nullable";
        public static final String PROP_DISCRIMINATOR = "discriminator";
        public static final String PROP_ANY_OF = "anyOf";
        public static final String PROP_ONE_OF = "oneOf";
        public static final String PROP_XML = "xml";
        public static final String PROP_READ_ONLY = "readOnly";
        public static final String PROP_ADDITIONAL_PROPERTIES = "additionalProperties";
        public static final String PROP_PROPERTIES = "properties";
        public static final String PROP_ALL_OF = "allOf";
        public static final String PROP_NOT = "not";
        public static final String PROP_ITEMS = "items";
        public static final String PROP_TYPE = "type";
        public static final String PROP_EXAMPLE = "example";
        public static final String PROP_EXTERNAL_DOCS = EXTERNAL_DOCS;
        public static final String PROP_DEPRECATED = "deprecated";
        public static final String PROP_HIDDEN = "hidden";

        // Only in SchemaFactory ?
        public static final String PROP_REQUIRED_PROPERTIES = "requiredProperties";
        public static final String PROP_REF = REF;
        public static final String PROP_DISCRIMINATOR_PROPERTY = "discriminatorProperty";
        public static final String PROP_DISCRIMINATOR_MAPPING = "discriminatorMapping";
        public static final String PROP_IMPLEMENTATION = "implementation";
        public static final String PROP_VALUE = VALUE;
        public static final String PROP_SCHEMA = "schema";
        // for annotations (reserved words in Java)
        public static final String PROP_ENUMERATION = "enumeration";
        public static final String PROP_DEFAULT_VALUE = "defaultValue";
    }

    // XML (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#xmlObject)
    public static class XML {
        public static final String PROP_NAME = NAME;
        public static final String PROP_NAMESPACE = "namespace";
        public static final String PROP_PREFIX = "prefix";
        public static final String PROP_WRAPPED = "wrapped";
        public static final String PROP_ATTRIBUTE = "attribute";
    }

    // Discriminator (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#discriminatorObject)
    public static class DISCRIMINATOR {
        public static final String PROP_MAPPING = "mapping";
        public static final String PROP_PROPERTY_NAME = "propertyName";
    }

    public static class CONTENT {
        public static final String PROP_CONTENT = "content";
    }

    // MediaType (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#mediaTypeObject)
    public static class MEDIATYPE {
        public static final String PROP_EXAMPLE = "example";
        public static final String PROP_EXAMPLES = "examples";
        public static final String PROP_SCHEMA = "schema";
        public static final String PROP_ENCODING = "encoding";
    }

    // Encoding (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#encodingObject)
    public static class ENCODING {
        public static final String PROP_NAME = NAME;
        public static final String PROP_CONTENT_TYPE = "contentType";
        public static final String PROP_STYLE = "style";
        public static final String PROP_EXPLODE = "explode";
        public static final String PROP_ALLOW_RESERVED = "allowReserved";
        public static final String PROP_HEADERS = "headers";
        public static final String PROP_ENCODING = "encoding";
    }

    // Link (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#linkObject)
    public static class LINK {
        public static final String PROP_LINKS = "links";
        public static final String PROP_NAME = NAME;
        public static final String PROP_REF_VAR = $REF;
        public static final String PROP_SERVER = "server";
        public static final String PROP_REQUEST_BODY = "requestBody";
        public static final String PROP_OPERATION_ID = "operationId";
        public static final String PROP_OPERATION_REF = "operationRef";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_PARAMETERS = "parameters";
        public static final String PROP_EXPRESSION = "expression";
    }

    // Parameter (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#parameter-object)
    public static class PARAMETER {
        public static final DotName TYPE_PARAMETER = DotName.createSimple(Parameter.class.getName());
        public static final DotName TYPE_PARAMETERS = DotName.createSimple(Parameters.class.getName());
        public static final String PROP_PARAMETERS = "parameters";
        public static final String PROP_NAME = NAME;
        public static final String PROP_REF_VAR = $REF;
        public static final String PROP_IN = "in";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_REQUIRED = "required";
        public static final String PROP_DEPRECATED = "deprecated";
        public static final String PROP_ALLOW_EMPTY_VALUE = "allowEmptyValue";
        public static final String PROP_STYLE = "style";
        public static final String PROP_EXPLODE = "explode";
        public static final String PROP_ALLOW_RESERVED = "allowReserved";
        public static final String PROP_SCHEMA = "schema";
        public static final String PROP_CONTENT = "content";
        public static final String PROP_EXAMPLE = "example";
        public static final String PROP_EXAMPLES = "examples";
        public static final String PROP_HIDDEN = "hidden";
        public static final String PROP_VALUE = VALUE;
    }

    // RequestBodyObject https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#requestBodyObject
    public static class REQUESTBODY {
        public static final DotName TYPE_REQUESTBODY = DotName.createSimple(RequestBody.class.getName());
        public static final String PROP_REQUEST_BODY = "requestBody";
        public static final String PROP_NAME = NAME;
        public static final String PROP_REF_VAR = $REF;
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_CONTENT = "content";
        public static final String PROP_REQUIRED = "required";
    }

    // Response (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#responseObject)
    public static class RESPONSE {
        public static final DotName TYPE_API_RESPONSE = DotName.createSimple(APIResponse.class.getName());
        public static final DotName TYPE_API_RESPONSES = DotName.createSimple(APIResponses.class.getName());

        public static final String PROP_RESPONSES = "responses";
        public static final String PROP_NAME = NAME;
        public static final String PROP_REF_VAR = $REF;
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_HEADERS = "headers";
        public static final String PROP_LINKS = "links";
        public static final String PROP_CONTENT = "content";
        public static final String PROP_RESPONSE_CODE = "responseCode";
        public static final String PROP_DEFAULT = "default";
    }

    // SecurityScheme (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#securitySchemeObject)
    public static class SECURITYSCHEME {
        public static final DotName TYPE_SECURITY_SCHEME = DotName.createSimple(SecurityScheme.class.getName());
        public static final DotName TYPE_SECURITY_SCHEMES = DotName.createSimple(SecuritySchemes.class.getName());
        public static final String PROP_NAME = NAME;
        public static final String PROP_REF_VAR = $REF;
        public static final String PROP_SECURITY_SCHEME_NAME = "securitySchemeName";
        public static final String PROP_TYPE = "type";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_API_KEY_NAME = "apiKeyName";
        public static final String PROP_IN = "in";
        public static final String PROP_SCHEME = "scheme";
        public static final String PROP_BEARER_FORMAT = "bearerFormat";
        public static final String PROP_FLOWS = "flows";
        public static final String PROP_OPEN_ID_CONNECT_URL = "openIdConnectUrl";
        // OAuth
        @SuppressWarnings("squid:S2068") // Instruct SonarCloud to ignore this as a potentially hard-coded credential
        public static final String PROP_PASSWORD = "password";
        public static final String PROP_IMPLICIT = "implicit";
        public static final String PROP_AUTHORIZATION_CODE = "authorizationCode";
        public static final String PROP_CLIENT_CREDENTIALS = "clientCredentials";
        public static final String PROP_REFRESH_URL = "refreshUrl";
        public static final String PROP_TOKEN_URL = "tokenUrl";
        public static final String PROP_AUTHORIZATION_URL = "authorizationUrl";
        public static final String PROP_SCOPES = "scopes";
    }

    // Extensions (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#specificationExtensions)
    public static class EXTENSIONS {
        public static final DotName TYPE_EXTENSION = DotName.createSimple(Extension.class.getName());
        public static final DotName TYPE_EXTENSIONS = DotName.createSimple(Extensions.class.getName());
        public static final String PROP_NAME = NAME;
        public static final String PROP_VALUE = VALUE;
        public static final String PROP_PARSE_VALUE = "parseValue";
        public static final String EXTENSION_PROPERTY_PREFIX = "x-";
    }

    // Operation (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#operationObject)
    public static class OPERATION {
        public static final DotName TYPE_OPERATION = DotName.createSimple(Operation.class.getName());
        public static final String PROP_SUMMARY = "summary";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_EXTERNAL_DOCS = EXTERNAL_DOCS;
        public static final String PROP_PARAMETERS = "parameters";
        public static final String PROP_REQUEST_BODY = "requestBody";
        public static final String PROP_RESPONSES = "responses";
        public static final String PROP_SECURITY = "security";
        public static final String PROP_TAGS = "tags";
        public static final String PROP_OPERATION_ID = "operationId";
        public static final String PROP_CALLBACKS = "callbacks";
        public static final String PROP_DEPRECATED = "deprecated";
        public static final String PROP_SERVERS = "servers";
        public static final String PROP_EXTENSIONS = "extensions";
        public static final String PROP_HIDDEN = "hidden";
    }

    // Path Item (https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#pathsObject)
    public static class PATHITEM {
        public static final String PROP_REF_VAR = $REF;
        public static final String PROP_SUMMARY = "summary";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_TRACE = "trace";
        public static final String PROP_PATCH = "patch";
        public static final String PROP_HEAD = "head";
        public static final String PROP_OPTIONS = "options";
        public static final String PROP_DELETE = "delete";
        public static final String PROP_POST = "post";
        public static final String PROP_PUT = "put";
        public static final String PROP_GET = "get";
        public static final String PROP_PARAMETERS = "parameters";
        public static final String PROP_SERVERS = "servers";
        public static final String PROP_METHOD = "method";
    }

    // Shared (private)
    private static final String NAME = "name";
    private static final String EXTERNAL_DOCS = "externalDocs";
    @SuppressWarnings("squid:S00115") // Instruct SonarCloud to ignore this unconventional variable name
    private static final String $REF = "$ref";

    // Shared public
    public static final String OPEN_API_VERSION = "3.0.1";
    public static final String VALUE = "value";
    public static final String REF = "ref";
    public static final String REFS = "refs";

    public static final Supplier<String[]> DEFAULT_MEDIA_TYPES = () -> new String[] { "*/*" };

    private MPOpenApiConstants() {
    }
}
