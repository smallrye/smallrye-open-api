package io.smallrye.openapi.runtime.io;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import com.fasterxml.jackson.databind.ObjectMapper;

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
     */
    public static String serialize(OpenAPI openApi, Format format) {
        return serialize(openApi, format, JsonIO.newInstance(null));
    }

    /**
     * Serializes the given OpenAPI object into either JSON or YAML and returns it as a string.
     *
     * @param openApi the OpenAPI object
     * @param objectMapper the {@link ObjectMapper} to use
     * @param format the serialization format
     * @return OpenAPI object as a String
     */
    public static String serialize(OpenAPI openApi, ObjectMapper objectMapper, Format format) {
        return serialize(openApi, format, JsonIO.newInstance(null));
    }

    private static <V, A extends V, O extends V, AB, OB> String serialize(OpenAPI openApi, Format format,
            JsonIO<V, A, O, AB, OB> jsonIO) {
        return new OpenAPIDefinitionIO<>(IOContext.forJson(jsonIO))
                .write(openApi)
                .map(node -> jsonIO.toString(node, format))
                .orElseThrow(IllegalStateException::new);
    }

}
