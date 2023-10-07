package io.smallrye.openapi.runtime.io;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import io.smallrye.openapi.runtime.io.definition.DefinitionWriter;

/**
 * Class used to serialize an OpenAPI
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApiSerializer {

    private OpenApiSerializer() {
    }

    /**
     * Creates an {@link ObjectWriter} for the given format with the appropriate settings.
     *
     * @param objectMapper the {@link ObjectMapper} to use
     * @param format the serialization format
     * @return the {@link ObjectWriter} with the appropriate settings
     */
    public static ObjectWriter createObjectWriter(ObjectMapper objectMapper, Format format) {
        if (format == Format.JSON) {
            return objectMapper.writerWithDefaultPrettyPrinter();
        } else {
            YAMLFactory factory = new YAMLFactory();
            factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
            factory.enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS);
            factory.enable(YAMLGenerator.Feature.ALLOW_LONG_KEYS);
            return objectMapper.writer().with(factory);
        }
    }

    /**
     * Serializes the given OpenAPI object into either JSON or YAML and returns it as a string.
     *
     * @param openApi the OpenAPI object
     * @param format the serialization format
     * @return OpenAPI object as a String
     * @throws IOException Errors in processing the JSON
     */
    public static String serialize(OpenAPI openApi, Format format) throws IOException {
        return serialize(openApi, new ObjectMapper(), format);
    }

    /**
     * Serializes the given OpenAPI object into either JSON or YAML and returns it as a string.
     *
     * @param openApi the OpenAPI object
     * @param objectMapper the {@link ObjectMapper} to use
     * @param format the serialization format
     * @return OpenAPI object as a String
     * @throws IOException Errors in processing the JSON
     */
    public static String serialize(OpenAPI openApi, ObjectMapper objectMapper, Format format) throws IOException {
        ObjectWriter writer = createObjectWriter(objectMapper, format);
        return serialize(openApi, writer);
    }

    /**
     * Serializes the given OpenAPI object using the provided {@link ObjectWriter} and returns it as a string.
     *
     * @param openApi the OpenAPI object
     * @param writer the {@link ObjectWriter} to use
     * @return OpenAPI object as a String
     * @throws IOException Errors in processing the model
     */
    public static String serialize(OpenAPI openApi, ObjectWriter writer) throws IOException {
        try {
            ObjectNode tree = JsonUtil.objectNode();
            DefinitionWriter.writeOpenAPI(tree, openApi);
            return writer.writeValueAsString(tree);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }
}
