package io.smallrye.openapi.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.OpenApiRuntimeException;
import io.smallrye.openapi.runtime.io.media.SchemaIO;

/**
 * A class used to parse an OpenAPI document (either YAML or JSON) into a Microprofile OpenAPI model tree.
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApiParser {

    private OpenApiParser() {
    }

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
                return parse(stream, isJson ? Format.JSON : Format.YAML, OpenApiConfig.fromConfig(ConfigProvider.getConfig()));
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
     */
    public static OpenAPI parse(InputStream stream, Format format, OpenApiConfig config) {
        return parse(stream, format, JsonIO.newInstance(config));
    }

    private static <V, A extends V, O extends V, AB, OB> OpenAPI parse(InputStream stream, Format format,
            JsonIO<V, A, O, AB, OB> jsonIO) {
        IOContext<V, A, O, AB, OB> context = IOContext.forJson(jsonIO);
        return new OpenAPIDefinitionIO<>(context).readValue(jsonIO.fromStream(stream, format));

    }

    /**
     * Parses the schema in the provided String. The format of the stream must
     * be JSON.
     *
     * @param schemaJson String containing a JSON formatted schema
     * @return Schema parsed from the String
     * @throws OpenApiRuntimeException Errors in reading the String
     */
    public static Schema parseSchema(String schemaJson) {
        return parseSchema(schemaJson, JsonIO.newInstance(null));
    }

    /**
     * Parses the schema in the provided String. The format of the stream must
     * be JSON.
     *
     * @param schemaJson String containing a JSON formatted schema
     * @param jsonIO JsonIO implementation for a low-level JSON reader/parser
     * @return Schema parsed from the String
     * @throws OpenApiRuntimeException Errors in reading the String
     */
    private static <V, A extends V, O extends V, AB, OB> Schema parseSchema(String schemaJson, JsonIO<V, A, O, AB, OB> jsonIO) {
        IOContext<V, A, O, AB, OB> context = IOContext.forJson(jsonIO);
        V schemaValue = jsonIO.fromString(schemaJson, Format.JSON);
        return new SchemaIO<V, A, O, AB, OB>(context).readValue(schemaValue);
    }

}
