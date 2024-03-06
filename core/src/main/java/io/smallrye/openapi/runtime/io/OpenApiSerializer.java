package io.smallrye.openapi.runtime.io;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class used to serialize an OpenAPI
 *
 * @author eric.wittmann@gmail.com
 *
 * @deprecated use the {@link io.smallrye.openapi.api.SmallRyeOpenAPI
 *             SmallRyeOpenAPI} builder API instead. This class may be moved,
 *             have reduced visibility, or be removed in a future release.
 */
@Deprecated
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
    public static String serialize(OpenAPI openApi, Format format) throws IOException {
        return serialize(openApi, format, JsonIO.newInstance(null));
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
        return serialize(openApi, format, JsonIO.newInstance(null));
    }

    private static <V, A extends V, O extends V, AB, OB> String serialize(OpenAPI openApi, Format format,
            JsonIO<V, A, O, AB, OB> jsonIO) throws IOException {
        return new OpenAPIDefinitionIO<>(IOContext.forJson(jsonIO))
                .write(openApi)
                .map(node -> jsonIO.toString(node, format))
                .orElseThrow(IOException::new);
    }

}
