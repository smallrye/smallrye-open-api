package io.smallrye.openapi.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.runtime.io.definition.DefinitionReader;
import io.smallrye.openapi.runtime.io.schema.SchemaReader;

/**
 * A class used to parse an OpenAPI document (either YAML or JSON) into a Microprofile OpenAPI model tree.
 * 
 * @author eric.wittmann@gmail.com
 */
public class OpenApiParser {

    /**
     * Parses the resource found at the given URL. This method accepts resources
     * either in JSON or YAML format. It will parse the input and, assuming it is
     * valid, return an instance of {@link OpenAPI}.
     * 
     * @param url URL to OpenAPI document
     * @return OpenAPIImpl parsed from URL
     * @throws IOException URL parameter is not found
     * @throws ParseException Failed to parse the document
     */
    public static final OpenAPI parse(URL url) throws IOException, ParseException {
        try {
            String fname = url.getFile();
            if (fname == null) {
                throw new IOException("No file name for URL: " + url.toURI().toString());
            }
            int lidx = fname.lastIndexOf('.');
            if (lidx == -1 || lidx >= fname.length()) {
                throw new IOException("Invalid file name for URL: " + url.toURI().toString());
            }
            String ext = fname.substring(lidx + 1);
            boolean isJson = ext.equalsIgnoreCase("json");
            boolean isYaml = ext.equalsIgnoreCase("yaml") || ext.equalsIgnoreCase("yml");
            if (!isJson && !isYaml) {
                throw new IOException(
                        "Invalid file extension for URL (expected json, yaml, or yml): " + url.toURI().toString());
            }

            try (InputStream stream = url.openStream()) {
                return parse(stream, isJson ? Format.JSON : Format.YAML);
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    /**
     * Parses the resource found at the given stream. The format of the stream must
     * be specified.
     * 
     * @param stream InputStream containing an OpenAPI document
     * @param format Format of the stream
     * @return OpenAPIImpl parsed from the stream
     * @throws IOException Errors in reading the stream
     */
    public static final OpenAPI parse(InputStream stream, Format format) throws IOException {
        ObjectMapper mapper;
        if (format == Format.JSON) {
            mapper = new ObjectMapper();
        } else {
            mapper = new ObjectMapper(new YAMLFactory());
        }
        JsonNode tree = mapper.readTree(stream);

        OpenApiParser parser = new OpenApiParser(tree);
        return parser.parse();
    }

    /**
     * Parses the schema in the provided String. The format of the stream must
     * be JSON.
     *
     * @param schemaJson String containing a JSON formatted schema
     * @return Schema parsed from the String
     * @throws IOException Errors in reading the String
     */
    public static final Schema parseSchema(String schemaJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree = mapper.readTree(schemaJson);
        return SchemaReader.readSchema(tree);
    }

    private final JsonNode tree;

    /**
     * Constructor.
     * 
     * @param tree JsonNode
     */
    public OpenApiParser(JsonNode tree) {
        this.tree = tree;
    }

    /**
     * Parses the json tree into an OpenAPI data model.
     */
    private OpenAPI parse() {
        OpenAPI oai = new OpenAPIImpl();
        DefinitionReader.processDefinition(oai, tree);
        return oai;
    }
}
