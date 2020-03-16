package io.smallrye.openapi.runtime.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.Extensible;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.io.callback.CallbackConstant;
import io.smallrye.openapi.runtime.io.components.ComponentsConstant;
import io.smallrye.openapi.runtime.io.contact.ContactConstant;
import io.smallrye.openapi.runtime.io.content.ContentConstant;
import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;
import io.smallrye.openapi.runtime.io.discriminator.DiscriminatorConstant;
import io.smallrye.openapi.runtime.io.encoding.EncodingConstant;
import io.smallrye.openapi.runtime.io.example.ExampleConstant;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsConstant;
import io.smallrye.openapi.runtime.io.header.HeaderConstant;
import io.smallrye.openapi.runtime.io.info.InfoConstant;
import io.smallrye.openapi.runtime.io.license.LicenseConstant;
import io.smallrye.openapi.runtime.io.link.LinkConstant;
import io.smallrye.openapi.runtime.io.mediatype.MediaTypeConstant;
import io.smallrye.openapi.runtime.io.operation.OperationConstant;
import io.smallrye.openapi.runtime.io.parameter.ParameterConstant;
import io.smallrye.openapi.runtime.io.paths.PathsConstant;
import io.smallrye.openapi.runtime.io.requestbody.RequestBodyConstant;
import io.smallrye.openapi.runtime.io.response.ResponseConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.securityscheme.SecuritySchemeConstant;
import io.smallrye.openapi.runtime.io.server.ServerConstant;
import io.smallrye.openapi.runtime.io.servervariable.ServerVariableConstant;
import io.smallrye.openapi.runtime.io.tag.TagConstant;
import io.smallrye.openapi.runtime.io.xml.XmlConstant;

/**
 * Class used to serialize an OpenAPI
 * 
 * @author eric.wittmann@gmail.com
 */
public class OpenApiSerializer {

    private final OpenAPI oai;

    /**
     * Constructor.
     * 
     * @param oai OpenAPI model
     */
    public OpenApiSerializer(OpenAPI oai) {
        this.oai = oai;
    }

    /**
     * Serializes the given OpenAPI object into either JSON or YAML and returns it as a string.
     * 
     * @param oai the OpenAPI object
     * @param format the serialization format
     * @return OpenAPI object as a String
     * @throws IOException Errors in processing the JSON
     */
    public static final String serialize(OpenAPI oai, Format format) throws IOException {
        try {
            OpenApiSerializer serializer = new OpenApiSerializer(oai);
            JsonNode tree = serializer.serialize();

            ObjectMapper mapper;
            if (format == Format.JSON) {
                mapper = new ObjectMapper();
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
            } else {
                YAMLFactory factory = new YAMLFactory();
                factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
                factory.enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS);
                mapper = new ObjectMapper(factory);
                return mapper.writer().writeValueAsString(tree);
            }
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    /**
     * Serializes the OAI model into a json/yaml tree.
     */
    private JsonNode serialize() {
        ObjectNode root = JsonUtil.objectNode();
        this.writeOpenAPI(root, this.oai);
        return root;
    }

    /**
     * Writes the given model.
     * 
     * @param node
     * @param model
     */
    private void writeOpenAPI(ObjectNode node, OpenAPI model) {
        JsonUtil.stringProperty(node, DefinitionConstant.PROP_OPENAPI, model.getOpenapi());
        writeInfo(node, model.getInfo());
        writeExternalDocumentation(node, model.getExternalDocs());
        writeServers(node, model.getServers());
        writeSecurity(node, model.getSecurity());
        writeTags(node, model.getTags());
        writePaths(node, model.getPaths());
        writeComponents(node, model.getComponents());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link Info} model to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeInfo(ObjectNode parent, Info model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(DefinitionConstant.PROP_INFO);

        JsonUtil.stringProperty(node, InfoConstant.PROP_TITLE, model.getTitle());
        JsonUtil.stringProperty(node, InfoConstant.PROP_DESCRIPTION, model.getDescription());
        JsonUtil.stringProperty(node, InfoConstant.PROP_TERMS_OF_SERVICE, model.getTermsOfService());
        writeContact(node, model.getContact());
        writeLicense(node, model.getLicense());
        JsonUtil.stringProperty(node, InfoConstant.PROP_VERSION, model.getVersion());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link Contact} model to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeContact(ObjectNode parent, Contact model) {
        if (model == null) {
            return;
        }
        ObjectNode node = JsonUtil.objectNode();
        parent.set(InfoConstant.PROP_CONTACT, node);

        JsonUtil.stringProperty(node, ContactConstant.PROP_NAME, model.getName());
        JsonUtil.stringProperty(node, ContactConstant.PROP_URL, model.getUrl());
        JsonUtil.stringProperty(node, ContactConstant.PROP_EMAIL, model.getEmail());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link License} model to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeLicense(ObjectNode parent, License model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(InfoConstant.PROP_LICENSE);

        JsonUtil.stringProperty(node, LicenseConstant.PROP_NAME, model.getName());
        JsonUtil.stringProperty(node, LicenseConstant.PROP_URL, model.getUrl());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link ExternalDocumentation} model to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeExternalDocumentation(ObjectNode parent, ExternalDocumentation model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(DefinitionConstant.PROP_EXTERNAL_DOCS);

        JsonUtil.stringProperty(node, ExternalDocsConstant.PROP_DESCRIPTION, model.getDescription());
        JsonUtil.stringProperty(node, ExternalDocsConstant.PROP_URL, model.getUrl());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link Tag} model array to the JSON tree.
     * 
     * @param node
     * @param tags
     */
    private void writeTags(ObjectNode node, List<Tag> tags) {
        if (tags == null) {
            return;
        }
        ArrayNode array = node.putArray(DefinitionConstant.PROP_TAGS);
        for (Tag tag : tags) {
            ObjectNode tagNode = array.addObject();
            JsonUtil.stringProperty(tagNode, TagConstant.PROP_NAME, tag.getName());
            JsonUtil.stringProperty(tagNode, TagConstant.PROP_DESCRIPTION, tag.getDescription());
            writeExternalDocumentation(tagNode, tag.getExternalDocs());
            writeExtensions(tagNode, tag);
        }
    }

    /**
     * Writes the {@link Server} model array to the JSON tree.
     * 
     * @param node
     * @param servers
     */
    private void writeServers(ObjectNode node, List<Server> servers) {
        if (servers == null) {
            return;
        }
        ArrayNode array = node.putArray(DefinitionConstant.PROP_SERVERS);
        for (Server server : servers) {
            ObjectNode serverNode = array.addObject();
            writeServerToNode(serverNode, server);
        }
    }

    /**
     * Writes a {@link Server} model to the given JSON node.
     * 
     * @param node ObjectNode
     * @param model Server
     */
    protected void writeServerToNode(ObjectNode node, Server model) {
        JsonUtil.stringProperty(node, ServerConstant.PROP_URL, model.getUrl());
        JsonUtil.stringProperty(node, ServerConstant.PROP_DESCRIPTION, model.getDescription());
        writeServerVariables(node, model.getVariables());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link ServerVariables} model to the JSON tree.
     * 
     * @param serverNode
     * @param variables
     */
    private void writeServerVariables(ObjectNode serverNode, Map<String, ServerVariable> variables) {
        if (variables == null) {
            return;
        }
        ObjectNode variablesNode = serverNode.putObject(ServerConstant.PROP_VARIABLES);
        write(variablesNode, variables, this::writeServerVariable);
    }

    /**
     * Writes a {@link ServerVariable} to the JSON tree.
     * 
     * @param parent
     * @param variableName
     * @param model
     */
    private void writeServerVariable(ObjectNode parent, ServerVariable model, String variableName) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(variableName);
        JsonUtil.stringProperty(node, ServerVariableConstant.PROP_DEFAULT, model.getDefaultValue());
        JsonUtil.stringProperty(node, ServerVariableConstant.PROP_DESCRIPTION, model.getDescription());
        List<String> enumeration = model.getEnumeration();
        if (enumeration != null) {
            ArrayNode enumArray = node.putArray(ServerVariableConstant.PROP_ENUM);
            for (String enumValue : enumeration) {
                enumArray.add(enumValue);
            }
        }
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link SecurityRequirement} model array to the JSON tree.
     * 
     * @param parent
     * @param security
     */
    private void writeSecurity(ObjectNode parent, List<SecurityRequirement> security) {
        if (security == null) {
            return;
        }
        ArrayNode array = parent.putArray(DefinitionConstant.PROP_SECURITY);
        for (SecurityRequirement securityRequirement : security) {
            ObjectNode srNode = array.addObject();
            for (Entry<String, List<String>> entry : securityRequirement.getSchemes().entrySet()) {
                String fieldName = entry.getKey();
                List<String> values = entry.getValue();
                ArrayNode valuesNode = srNode.putArray(fieldName);
                if (values != null) {
                    for (String value : values) {
                        valuesNode.add(value);
                    }
                }
            }
        }
    }

    /**
     * Writes a {@link Paths} to the JSON tree.
     * 
     * @param parent
     * @param paths
     */
    private void writePaths(ObjectNode parent, Paths paths) {
        if (paths == null) {
            return;
        }
        ObjectNode pathsNode = parent.putObject(DefinitionConstant.PROP_PATHS);
        write(pathsNode, paths.getPathItems(), this::writePathItem);
        writeExtensions(pathsNode, paths);
    }

    /**
     * Writes a {@link PathItem} to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param pathName
     */
    private void writePathItem(ObjectNode parent, PathItem model, String pathName) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(pathName);
        JsonUtil.stringProperty(node, PathsConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, PathsConstant.PROP_SUMMARY, model.getSummary());
        JsonUtil.stringProperty(node, PathsConstant.PROP_DESCRIPTION, model.getDescription());
        writeOperation(node, model.getGET(), PathsConstant.PROP_GET);
        writeOperation(node, model.getPUT(), PathsConstant.PROP_PUT);
        writeOperation(node, model.getPOST(), PathsConstant.PROP_POST);
        writeOperation(node, model.getDELETE(), PathsConstant.PROP_DELETE);
        writeOperation(node, model.getOPTIONS(), PathsConstant.PROP_OPTIONS);
        writeOperation(node, model.getHEAD(), PathsConstant.PROP_HEAD);
        writeOperation(node, model.getPATCH(), PathsConstant.PROP_PATCH);
        writeOperation(node, model.getTRACE(), PathsConstant.PROP_TRACE);
        writeParameterList(node, model.getParameters());
        writeServers(node, model.getServers());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link Operation} to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param method
     */
    private void writeOperation(ObjectNode parent, Operation model, String method) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(method);
        writeStringArray(node, model.getTags(), OperationConstant.PROP_TAGS);
        JsonUtil.stringProperty(node, OperationConstant.PROP_SUMMARY, model.getSummary());
        JsonUtil.stringProperty(node, OperationConstant.PROP_DESCRIPTION, model.getDescription());
        writeExternalDocumentation(node, model.getExternalDocs());
        JsonUtil.stringProperty(node, OperationConstant.PROP_OPERATION_ID, model.getOperationId());
        writeParameterList(node, model.getParameters());
        writeRequestBody(node, model.getRequestBody());
        writeAPIResponses(node, model.getResponses());
        writeCallbacks(node, model.getCallbacks());
        JsonUtil.booleanProperty(node, OperationConstant.PROP_DEPRECATED, model.getDeprecated());
        writeSecurityRequirements(node, model.getSecurity());
        writeServers(node, model.getServers());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link RequestBody} to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeRequestBody(ObjectNode parent, RequestBody model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(RequestBodyConstant.PROP_REQUEST_BODY);
        JsonUtil.stringProperty(node, RequestBodyConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, RequestBodyConstant.PROP_DESCRIPTION, model.getDescription());
        writeContent(node, model.getContent());
        JsonUtil.booleanProperty(node, RequestBodyConstant.PROP_REQUIRED, model.getRequired());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link Content} to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeContent(ObjectNode parent, Content model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(ContentConstant.PROP_CONTENT);
        write(node, model.getMediaTypes(), this::writeMediaType);
    }

    /**
     * Writes a {@link MediaType} to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeMediaType(ObjectNode parent, MediaType model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        writeSchema(node, model.getSchema(), MediaTypeConstant.PROP_SCHEMA);
        writeObject(node, MediaTypeConstant.PROP_EXAMPLE, model.getExample());
        writeExamples(node, model.getExamples());
        writeEncodings(node, model.getEncoding());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link Schema} to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeSchema(ObjectNode parent, Schema model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        writeSchemaToNode(node, model);
    }

    /**
     * Writes the {@link Schema} model to the given node.
     * 
     * @param node
     * @param model
     */
    private void writeSchemaToNode(ObjectNode node, Schema model) {
        JsonUtil.stringProperty(node, SchemaConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, SchemaConstant.PROP_FORMAT, model.getFormat());
        JsonUtil.stringProperty(node, SchemaConstant.PROP_TITLE, model.getTitle());
        JsonUtil.stringProperty(node, SchemaConstant.PROP_DESCRIPTION, model.getDescription());
        writeObject(node, SchemaConstant.PROP_DEFAULT, model.getDefaultValue());
        JsonUtil.bigDecimalProperty(node, SchemaConstant.PROP_MULTIPLE_OF, model.getMultipleOf());
        JsonUtil.bigDecimalProperty(node, SchemaConstant.PROP_MAXIMUM, model.getMaximum());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_EXCLUSIVE_MAXIMUM, model.getExclusiveMaximum());
        JsonUtil.bigDecimalProperty(node, SchemaConstant.PROP_MINIMUM, model.getMinimum());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_EXCLUSIVE_MINIMUM, model.getExclusiveMinimum());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MAX_LENGTH, model.getMaxLength());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MIN_LENGTH, model.getMinLength());
        JsonUtil.stringProperty(node, SchemaConstant.PROP_PATTERN, model.getPattern());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MAX_ITEMS, model.getMaxItems());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MIN_ITEMS, model.getMinItems());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_UNIQUE_ITEMS, model.getUniqueItems());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MAX_PROPERTIES, model.getMaxProperties());
        JsonUtil.intProperty(node, SchemaConstant.PROP_MIN_PROPERTIES, model.getMinProperties());
        writeStringArray(node, model.getRequired(), SchemaConstant.PROP_REQUIRED);
        writeObjectArray(node, model.getEnumeration(), SchemaConstant.PROP_ENUM);
        JsonUtil.enumProperty(node, SchemaConstant.PROP_TYPE, model.getType());
        writeSchema(node, model.getItems(), SchemaConstant.PROP_ITEMS);
        writeSchemaList(node, model.getAllOf(), SchemaConstant.PROP_ALL_OF);
        writeSchemas(node, model.getProperties(), SchemaConstant.PROP_PROPERTIES);
        if (model.getAdditionalPropertiesBoolean() != null) {
            JsonUtil.booleanProperty(node, SchemaConstant.PROP_ADDITIONAL_PROPERTIES,
                    model.getAdditionalPropertiesBoolean());
        } else {
            writeSchema(node, (Schema) model.getAdditionalPropertiesSchema(),
                    SchemaConstant.PROP_ADDITIONAL_PROPERTIES);
        }
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_READ_ONLY, model.getReadOnly());
        writeXML(node, model.getXml());
        writeExternalDocumentation(node, model.getExternalDocs());
        writeObject(node, SchemaConstant.PROP_EXAMPLE, model.getExample());
        writeSchemaList(node, model.getOneOf(), SchemaConstant.PROP_ONE_OF);
        writeSchemaList(node, model.getAnyOf(), SchemaConstant.PROP_ANY_OF);
        writeSchema(node, model.getNot(), SchemaConstant.PROP_NOT);
        writeDiscriminator(node, model.getDiscriminator());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_NULLABLE, model.getNullable());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_WRITE_ONLY, model.getWriteOnly());
        JsonUtil.booleanProperty(node, SchemaConstant.PROP_DEPRECATED, model.getDeprecated());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link XML} object to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeXML(ObjectNode parent, XML model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(SchemaConstant.PROP_XML);
        JsonUtil.stringProperty(node, XmlConstant.PROP_NAME, model.getName());
        JsonUtil.stringProperty(node, XmlConstant.PROP_NAMESPACE, model.getNamespace());
        JsonUtil.stringProperty(node, XmlConstant.PROP_PREFIX, model.getPrefix());
        JsonUtil.booleanProperty(node, XmlConstant.PROP_ATTRIBUTE, model.getAttribute());
        JsonUtil.booleanProperty(node, XmlConstant.PROP_WRAPPED, model.getWrapped());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link Discriminator} object to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeDiscriminator(ObjectNode parent, Discriminator model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(SchemaConstant.PROP_DISCRIMINATOR);
        JsonUtil.stringProperty(node, DiscriminatorConstant.PROP_PROPERTY_NAME, model.getPropertyName());
        writeStringMap(node, model.getMapping(), DiscriminatorConstant.PROP_MAPPING);
    }

    /**
     * Writes a map of {@link Encoding} objects to the JSON tree.
     * 
     * @param parent
     * @param models
     */
    private void writeEncodings(ObjectNode parent, Map<String, Encoding> models) {
        if (models == null) {
            return;
        }
        ObjectNode node = parent.putObject(EncodingConstant.PROP_ENCODING);
        for (String name : models.keySet()) {
            Encoding encoding = models.get(name);
            writeEncoding(node, encoding, name);
        }
    }

    /**
     * Writes a {@link Encoding} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeEncoding(ObjectNode parent, Encoding model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, EncodingConstant.PROP_CONTENT_TYPE, model.getContentType());
        writeHeaders(node, model.getHeaders());
        JsonUtil.enumProperty(node, EncodingConstant.PROP_STYLE, model.getStyle());
        JsonUtil.booleanProperty(node, EncodingConstant.PROP_EXPLODE, model.getExplode());
        JsonUtil.booleanProperty(node, EncodingConstant.PROP_ALLOW_RESERVED, model.getAllowReserved());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link APIResponses} map to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeAPIResponses(ObjectNode parent, APIResponses model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(ResponseConstant.PROP_RESPONSES);
        writeAPIResponse(node, model.getDefaultValue(), ResponseConstant.PROP_DEFAULT);
        write(node, model.getAPIResponses(), this::writeAPIResponse);
    }

    /**
     * Writes a {@link APIResponse} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeAPIResponse(ObjectNode parent, APIResponse model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, ResponseConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, ResponseConstant.PROP_DESCRIPTION, model.getDescription());
        writeHeaders(node, model.getHeaders());
        writeContent(node, model.getContent());
        writeLinks(node, model.getLinks());
        writeExtensions(node, model);
    }

    /**
     * Writes a list of {@link SecurityRequirement} to the JSON tree.
     * 
     * @param parent
     * @param models
     */
    private void writeSecurityRequirements(ObjectNode parent, List<SecurityRequirement> models) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray(DefinitionConstant.PROP_SECURITY);
        for (SecurityRequirement securityRequirement : models) {
            ObjectNode secNode = node.addObject();
            writeSecurityRequirementToNode(secNode, securityRequirement);
        }
    }

    /**
     * Writes a {@link SecurityRequirement} to the given Json node.
     * 
     * @param node
     * @param model
     */
    private void writeSecurityRequirementToNode(ObjectNode node, SecurityRequirement model) {
        if (model == null) {
            return;
        }
        write(node, model.getSchemes(), this::writeStringArray);
    }

    /**
     * Writes a list of {@link Parameter} to the JSON tree.
     * 
     * @param parent
     * @param models
     */
    private void writeParameterList(ObjectNode parent, List<Parameter> models) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray(ParameterConstant.PROP_PARAMETERS);
        for (Parameter model : models) {
            ObjectNode paramNode = node.addObject();
            writeParameterToNode(paramNode, model);
        }
    }

    /**
     * Writes a {@link Parameter} into the JSON node.
     * 
     * @param node
     * @param model
     */
    private void writeParameterToNode(ObjectNode node, Parameter model) {
        JsonUtil.stringProperty(node, ParameterConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, ParameterConstant.PROP_NAME, model.getName());
        JsonUtil.enumProperty(node, ParameterConstant.PROP_IN, model.getIn());
        JsonUtil.stringProperty(node, ParameterConstant.PROP_DESCRIPTION, model.getDescription());
        JsonUtil.booleanProperty(node, ParameterConstant.PROP_REQUIRED, model.getRequired());
        writeSchema(node, model.getSchema(), ParameterConstant.PROP_SCHEMA);
        JsonUtil.booleanProperty(node, ParameterConstant.PROP_ALLOW_EMPTY_VALUE, model.getAllowEmptyValue());
        JsonUtil.booleanProperty(node, ParameterConstant.PROP_DEPRECATED, model.getDeprecated());
        JsonUtil.enumProperty(node, ParameterConstant.PROP_STYLE, model.getStyle());
        JsonUtil.booleanProperty(node, ParameterConstant.PROP_EXPLODE, model.getExplode());
        JsonUtil.booleanProperty(node, ParameterConstant.PROP_ALLOW_RESERVED, model.getAllowReserved());
        writeObject(node, ParameterConstant.PROP_EXAMPLE, model.getExample());
        writeExamples(node, model.getExamples());
        writeContent(node, model.getContent());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link ServerVariable} to the JSON tree.
     * 
     * @param parent
     * @param components
     */
    private void writeComponents(ObjectNode parent, Components components) {
        if (components == null) {
            return;
        }
        ObjectNode node = parent.putObject(DefinitionConstant.PROP_COMPONENTS);
        writeSchemas(node, components.getSchemas());
        writeResponses(node, components.getResponses());
        writeParameters(node, components.getParameters());
        writeExamples(node, components.getExamples());
        writeRequestBodies(node, components.getRequestBodies());
        writeHeaders(node, components.getHeaders());
        writeSecuritySchemes(node, components.getSecuritySchemes());
        writeLinks(node, components.getLinks());
        writeCallbacks(node, components.getCallbacks());
        writeExtensions(node, components);
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * 
     * @param parent
     * @param schemas
     */
    private void writeSchemas(ObjectNode parent, Map<String, Schema> schemas) {
        writeSchemas(parent, schemas, ComponentsConstant.PROP_SCHEMAS);
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * 
     * @param parent
     * @param schemas
     */
    private void writeSchemas(ObjectNode parent, Map<String, Schema> schemas, String propertyName) {
        if (schemas == null) {
            return;
        }
        ObjectNode schemasNode = parent.putObject(propertyName);
        for (String schemaName : schemas.keySet()) {
            writeSchema(schemasNode, schemas.get(schemaName), schemaName);
        }
    }

    /**
     * Writes a list of {@link Schema} to the JSON tree.
     * 
     * @param parent
     * @param models
     * @param propertyName
     */
    private void writeSchemaList(ObjectNode parent, List<Schema> models, String propertyName) {
        if (models == null) {
            return;
        }
        ArrayNode schemasNode = parent.putArray(propertyName);
        for (Schema schema : models) {
            writeSchemaToNode(schemasNode.addObject(), schema);
        }
    }

    /**
     * Writes a map of {@link APIResponse} to the JSON tree.
     * 
     * @param parent
     * @param responses
     */
    private void writeResponses(ObjectNode parent, Map<String, APIResponse> responses) {
        if (responses == null) {
            return;
        }
        ObjectNode responsesNode = parent.putObject(ResponseConstant.PROP_RESPONSES);
        for (String responseName : responses.keySet()) {
            writeAPIResponse(responsesNode, responses.get(responseName), responseName);
        }
    }

    /**
     * Writes a map of {@link Parameter} to the JSON tree.
     * 
     * @param parent
     * @param parameters
     */
    private void writeParameters(ObjectNode parent, Map<String, Parameter> parameters) {
        if (parameters == null) {
            return;
        }
        ObjectNode parametersNode = parent.putObject(ParameterConstant.PROP_PARAMETERS);
        for (String parameterName : parameters.keySet()) {
            writeParameter(parametersNode, parameters.get(parameterName), parameterName);
        }
    }

    /**
     * Writes a {@link Parameter} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeParameter(ObjectNode parent, Parameter model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        writeParameterToNode(node, model);
    }

    /**
     * Writes a map of {@link Example} to the JSON tree.
     * 
     * @param parent
     * @param examples
     */
    private void writeExamples(ObjectNode parent, Map<String, Example> examples) {
        if (examples == null) {
            return;
        }
        ObjectNode examplesNode = parent.putObject(ExampleConstant.PROP_EXAMPLES);
        for (String exampleName : examples.keySet()) {
            writeExample(examplesNode, examples.get(exampleName), exampleName);
        }
    }

    /**
     * Writes a {@link Example} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeExample(ObjectNode parent, Example model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, ExampleConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, ExampleConstant.PROP_SUMMARY, model.getSummary());
        JsonUtil.stringProperty(node, ExampleConstant.PROP_DESCRIPTION, model.getDescription());
        writeObject(node, ExampleConstant.PROP_VALUE, model.getValue());
        JsonUtil.stringProperty(node, ExampleConstant.PROP_EXTERNAL_VALUE, model.getExternalValue());
        writeExtensions(node, model);
    }

    /**
     * Writes a map of {@link RequestBody} to the JSON tree.
     * 
     * @param parent
     * @param requestBodies
     */
    private void writeRequestBodies(ObjectNode parent, Map<String, RequestBody> requestBodies) {
        if (requestBodies == null) {
            return;
        }
        ObjectNode requestBodiesNode = parent.putObject(ComponentsConstant.PROP_REQUEST_BODIES);
        for (String requestBodyName : requestBodies.keySet()) {
            writeRequestBody(requestBodiesNode, requestBodies.get(requestBodyName), requestBodyName);
        }
    }

    /**
     * Writes a {@link RequestBody} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeRequestBody(ObjectNode parent, RequestBody model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, RequestBodyConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, RequestBodyConstant.PROP_DESCRIPTION, model.getDescription());
        writeContent(node, model.getContent());
        JsonUtil.booleanProperty(node, RequestBodyConstant.PROP_REQUIRED, model.getRequired());
        writeExtensions(node, model);
    }

    /**
     * Writes a map of {@link Header} to the JSON tree.
     * 
     * @param parent
     * @param headers
     */
    private void writeHeaders(ObjectNode parent, Map<String, Header> headers) {
        if (headers == null) {
            return;
        }
        ObjectNode headersNode = parent.putObject(HeaderConstant.PROP_HEADERS);
        for (String headerName : headers.keySet()) {
            writeHeader(headersNode, headers.get(headerName), headerName);
        }
    }

    /**
     * Writes a {@link RequestBody} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeHeader(ObjectNode parent, Header model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, HeaderConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, HeaderConstant.PROP_DESCRIPTION, model.getDescription());
        JsonUtil.booleanProperty(node, HeaderConstant.PROP_REQUIRED, model.getRequired());
        JsonUtil.booleanProperty(node, HeaderConstant.PROP_DEPRECATED, model.getDeprecated());
        JsonUtil.booleanProperty(node, HeaderConstant.PROP_ALLOW_EMPTY_VALUE, model.getAllowEmptyValue());
        JsonUtil.enumProperty(node, HeaderConstant.PROP_STYLE, model.getStyle());
        JsonUtil.booleanProperty(node, HeaderConstant.PROP_EXPLODE, model.getExplode());
        writeSchema(node, model.getSchema(), HeaderConstant.PROP_SCHEMA);
        writeObject(node, HeaderConstant.PROP_EXAMPLE, model.getExample());
        writeExamples(node, model.getExamples());
        writeContent(node, model.getContent());
        writeExtensions(node, model);

    }

    /**
     * Writes a map of {@link SecurityScheme} to the JSON tree.
     * 
     * @param parent
     * @param securitySchemes
     */
    private void writeSecuritySchemes(ObjectNode parent, Map<String, SecurityScheme> securitySchemes) {
        if (securitySchemes == null) {
            return;
        }
        ObjectNode securitySchemesNode = parent.putObject(ComponentsConstant.PROP_SECURITY_SCHEMES);
        for (String securitySchemeName : securitySchemes.keySet()) {
            writeSecurityScheme(securitySchemesNode, securitySchemes.get(securitySchemeName), securitySchemeName);
        }
    }

    /**
     * Writes a {@link SecurityScheme} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeSecurityScheme(ObjectNode parent, SecurityScheme model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_$REF, model.getRef());
        JsonUtil.enumProperty(node, SecuritySchemeConstant.PROP_TYPE, model.getType());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_DESCRIPTION, model.getDescription());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_NAME, model.getName());
        JsonUtil.enumProperty(node, SecuritySchemeConstant.PROP_IN, model.getIn());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_SCHEME, model.getScheme());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_BEARER_FORMAT, model.getBearerFormat());
        writeOAuthFlows(node, model.getFlows());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_OPEN_ID_CONNECT_URL, model.getOpenIdConnectUrl());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link OAuthFlows} object to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeOAuthFlows(ObjectNode parent, OAuthFlows model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(SecuritySchemeConstant.PROP_FLOWS);
        writeOAuthFlow(node, model.getImplicit(), SecuritySchemeConstant.PROP_IMPLICIT);
        writeOAuthFlow(node, model.getPassword(), SecuritySchemeConstant.PROP_PASSWORD);
        writeOAuthFlow(node, model.getClientCredentials(), SecuritySchemeConstant.PROP_CLIENT_CREDENTIALS);
        writeOAuthFlow(node, model.getAuthorizationCode(), SecuritySchemeConstant.PROP_AUTHORIZATION_CODE);
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link OAuthFlow} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeOAuthFlow(ObjectNode parent, OAuthFlow model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_AUTHORIZATION_URL, model.getAuthorizationUrl());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_TOKEN_URL, model.getTokenUrl());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_REFRESH_URL, model.getRefreshUrl());
        writeStringMap(node, model.getScopes(), SecuritySchemeConstant.PROP_SCOPES);
        writeExtensions(node, model);
    }

    /**
     * Writes a map of {@link Link} to the JSON tree.
     * 
     * @param parent
     * @param links
     */
    private void writeLinks(ObjectNode parent, Map<String, Link> links) {
        if (links == null) {
            return;
        }
        ObjectNode linksNode = parent.putObject(LinkConstant.PROP_LINKS);
        for (String linkName : links.keySet()) {
            writeLink(linksNode, links.get(linkName), linkName);
        }
    }

    /**
     * Writes a {@link Link} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeLink(ObjectNode parent, Link model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, LinkConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPERATION_REF, model.getOperationRef());
        JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPERATION_ID, model.getOperationId());
        writeLinkParameters(node, model.getParameters());
        writeObject(node, LinkConstant.PROP_REQUEST_BODY, model.getRequestBody());
        JsonUtil.stringProperty(node, LinkConstant.PROP_DESCRIPTION, model.getDescription());
        writeServer(node, model.getServer());
        writeExtensions(node, model);
    }

    /**
     * Writes the link parameters to the given node.
     * 
     * @param parent
     * @param parameters
     */
    private void writeLinkParameters(ObjectNode parent, Map<String, Object> parameters) {
        if (parameters == null) {
            return;
        }
        ObjectNode node = parent.putObject(LinkConstant.PROP_PARAMETERS);
        for (String name : parameters.keySet()) {
            writeObject(node, name, parameters.get(name));
        }
    }

    /**
     * Writes a {@link Server} object to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private void writeServer(ObjectNode parent, Server model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(OpenApiConstants.PROP_SERVER);
        writeServerToNode(node, model);
    }

    /**
     * Writes a map of {@link Callback} to the JSON tree.
     * 
     * @param parent
     * @param callbacks
     */
    private void writeCallbacks(ObjectNode parent, Map<String, Callback> callbacks) {
        if (callbacks == null) {
            return;
        }
        ObjectNode callbacksNode = parent.putObject(CallbackConstant.PROP_CALLBACKS);
        write(callbacksNode, callbacks, this::writeCallback);
    }

    /**
     * Writes a {@link Callback} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private void writeCallback(ObjectNode parent, Callback model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, CallbackConstant.PROP_$REF, model.getRef());
        write(node, model.getPathItems(), this::writePathItem);
        writeExtensions(node, model);
    }

    /**
     * Writes extensions to the JSON tree.
     * 
     * @param node
     * @param model
     */
    private void writeExtensions(ObjectNode node, Extensible<?> model) {
        Map<String, Object> extensions = model.getExtensions();
        if (extensions == null || extensions.isEmpty()) {
            return;
        }
        for (Entry<String, Object> entry : extensions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            writeObject(node, key, value);
        }
    }

    /**
     * Writes an array of strings to the parent node.
     * 
     * @param parent
     * @param models
     * @param propertyName
     */
    private void writeStringArray(ObjectNode parent, List<String> models, String propertyName) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray(propertyName);
        for (String model : models) {
            node.add(model);
        }
    }

    /**
     * Writes an array of objects to the parent node.
     * 
     * @param parent
     * @param models
     * @param propertyName
     */
    private void writeObjectArray(ObjectNode parent, List<Object> models, String propertyName) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray(propertyName);
        for (Object model : models) {
            addObject(node, model);
        }
    }

    /**
     * Writes a map of strings to the parent node.
     * 
     * @param parent
     * @param models
     * @param propertyName
     */
    private void writeStringMap(ObjectNode parent, Map<String, String> models, String propertyName) {
        if (models == null) {
            return;
        }
        ObjectNode node = parent.putObject(propertyName);
        for (String name : models.keySet()) {
            String value = models.get(name);
            node.put(name, value);
        }
    }

    /**
     * @param node ObjectNode
     * @param key String
     * @param value Object
     */
    @SuppressWarnings("unchecked")
    protected void writeObject(ObjectNode node, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String) {
            node.put(key, (String) value);
        } else if (value instanceof JsonNode) {
            node.set(key, (JsonNode) value);
        } else if (value instanceof BigDecimal) {
            node.put(key, (BigDecimal) value);
        } else if (value instanceof BigInteger) {
            node.put(key, new BigDecimal((BigInteger) value));
        } else if (value instanceof Boolean) {
            node.put(key, (Boolean) value);
        } else if (value instanceof Double) {
            node.put(key, (Double) value);
        } else if (value instanceof Float) {
            node.put(key, (Float) value);
        } else if (value instanceof Integer) {
            node.put(key, (Integer) value);
        } else if (value instanceof Long) {
            node.put(key, (Long) value);
        } else if (value instanceof List) {
            ArrayNode array = node.putArray(key);
            List<Object> values = (List<Object>) value;
            for (Object valueItem : values) {
                addObject(array, valueItem);
            }
        } else if (value instanceof Map) {
            ObjectNode objNode = node.putObject(key);
            Map<String, Object> values = (Map<String, Object>) value;
            for (Entry<String, Object> entry : values.entrySet()) {
                String propertyName = entry.getKey();
                writeObject(objNode, propertyName, entry.getValue());
            }
        } else {
            node.put(key, (String) null);
        }
    }

    /**
     * Adds an object to an array.
     * 
     * @param node
     * @param value
     */
    @SuppressWarnings("unchecked")
    private void addObject(ArrayNode node, Object value) {
        if (value instanceof String) {
            node.add((String) value);
        } else if (value instanceof JsonNode) {
            node.add((JsonNode) value);
        } else if (value instanceof BigDecimal) {
            node.add((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            node.add(new BigDecimal((BigInteger) value));
        } else if (value instanceof Boolean) {
            node.add((Boolean) value);
        } else if (value instanceof Double) {
            node.add((Double) value);
        } else if (value instanceof Float) {
            node.add((Float) value);
        } else if (value instanceof Integer) {
            node.add((Integer) value);
        } else if (value instanceof Long) {
            node.add((Long) value);
        } else if (value instanceof List) {
            ArrayNode array = node.addArray();
            List<Object> values = (List<Object>) value;
            for (Object valueItem : values) {
                addObject(array, valueItem);
            }
        } else if (value instanceof Map) {
            ObjectNode objNode = node.addObject();
            Map<String, Object> values = (Map<String, Object>) value;
            for (Entry<String, Object> entry : values.entrySet()) {
                String propertyName = entry.getKey();
                writeObject(objNode, propertyName, entry.getValue());
            }
        } else {
            node.add((String) null);
        }
    }

    <T> void write(ObjectNode node, Map<String, T> map, NodeWriter<T> writer) {
        if (map != null) {
            map.entrySet().forEach(entry -> writer.write(node, entry.getValue(), entry.getKey()));
        }
    }
}
