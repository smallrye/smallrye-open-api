package io.smallrye.openapi.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.yaml.snakeyaml.LoaderOptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;

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
     */
    public static final OpenAPI parse(URL url) throws IOException {
        try {
            String fname = url.getFile();
            if (fname == null) {
                throw IoMessages.msg.noFileName(url.toURI().toString());
            }
            int lidx = fname.lastIndexOf('.');
            if (lidx == -1 || lidx >= fname.length()) {
                throw IoMessages.msg.invalidFileName(url.toURI().toString());
            }
            String ext = fname.substring(lidx + 1);
            boolean isJson = ext.equalsIgnoreCase("json");
            boolean isYaml = ext.equalsIgnoreCase("yaml") || ext.equalsIgnoreCase("yml");
            if (!isJson && !isYaml) {
                throw IoMessages.msg.invalidFileExtension(url.toURI().toString());
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
     * @param maximumStaticFileSize Integer to change (usually to increase) the maximum static file size
     * @return OpenAPIImpl parsed from the stream
     * @throws IOException Errors in reading the stream
     */
    public static final OpenAPI parse(InputStream stream, Format format, final Integer maximumStaticFileSize)
            throws IOException {
        ObjectMapper mapper;
        if (format == Format.JSON) {
            mapper = new ObjectMapper();
        } else {
            LoaderOptions loaderOptions = new LoaderOptions();
            if (maximumStaticFileSize != null) {
                loaderOptions.setCodePointLimit(maximumStaticFileSize);
            }
            mapper = new ObjectMapper(new YAMLFactoryBuilder(new YAMLFactory()).loaderOptions(loaderOptions).build());
        }
        JsonNode tree = mapper.readTree(stream);

        OpenApiParser parser = new OpenApiParser(tree);
        return parser.parse();
    }

    public static final OpenAPI parse(InputStream stream, Format format) throws IOException {
        return parse(stream, format, null);
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
