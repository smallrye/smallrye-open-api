package io.smallrye.openapi.runtime.io;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
     * Serializes the given OpenAPI object into either JSON or YAML and returns it as a string.
     *
     * @param openApi the OpenAPI object
     * @param format the serialization format
     * @return OpenAPI object as a String
     * @throws IOException Errors in processing the JSON
     */
    public static final String serialize(OpenAPI openApi, Format format) throws IOException {

        try {
            ObjectNode tree = JsonUtil.objectNode();
            DefinitionWriter.writeOpenAPI(tree, openApi);

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

}
