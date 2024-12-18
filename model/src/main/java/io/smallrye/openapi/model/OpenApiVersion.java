package io.smallrye.openapi.model;

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